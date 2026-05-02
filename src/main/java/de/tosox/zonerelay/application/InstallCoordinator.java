package de.tosox.zonerelay.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.model.EntryType;
import de.tosox.zonerelay.domain.model.HashMismatch;
import de.tosox.zonerelay.domain.model.Mod;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.model.ModlistConfig;
import de.tosox.zonerelay.domain.model.SetupFailure;
import de.tosox.zonerelay.domain.model.SetupPathMissingException;
import de.tosox.zonerelay.domain.port.*;
import de.tosox.zonerelay.infrastructure.download.DownloadsManifestStore;
import de.tosox.zonerelay.infrastructure.download.ManifestEntry;
import de.tosox.zonerelay.infrastructure.persistence.ModlistHashUpdater;
import de.tosox.zonerelay.infrastructure.persistence.ModlistSetupUpdater;
import de.tosox.zonerelay.shared.config.AppPaths;
import de.tosox.zonerelay.shared.config.ArchiveCleanupStrategy;
import de.tosox.zonerelay.shared.config.UserSettings;
import de.tosox.zonerelay.shared.i18n.Localizer;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.progress.ProgressListener;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Singleton
public class InstallCoordinator {
	private final Logger fileLogger;
	private final Logger uiLogger;
	private final Localizer localizer;
	private final List<ModInstaller> installers;
	private final ArchiveDownloader archiveDownloader;
	private final ProfileSetup profileSetup;
	private final SplashImageCopier splashImageCopier;
	private final ShortcutCreator shortcutCreator;
	private final InstallProgressStore progressStore;
	private final AppPaths paths;
	private final UserSettings userSettings;
	private final DownloadsManifestStore manifestStore;
	private final ModlistHashUpdater modlistHashUpdater;
	private final ModlistSetupUpdater modlistSetupUpdater;

	private final AtomicBoolean isInstalling = new AtomicBoolean(false);

	@Setter
	private ProgressListener currentProgressListener;

	@Setter
	private ProgressListener totalProgressListener;

	@Inject
	public InstallCoordinator(@Named("file") Logger fileLogger, @Named("ui") Logger uiLogger,
	                          Localizer localizer, List<ModInstaller> installers,
	                          ArchiveDownloader archiveDownloader, ProfileSetup profileSetup,
	                          SplashImageCopier splashImageCopier, ShortcutCreator shortcutCreator,
	                          InstallProgressStore progressStore, AppPaths paths,
	                          UserSettings userSettings, DownloadsManifestStore manifestStore,
	                          ModlistHashUpdater modlistHashUpdater, ModlistSetupUpdater modlistSetupUpdater) {
		this.fileLogger = fileLogger;
		this.uiLogger = uiLogger;
		this.localizer = localizer;
		this.installers = installers;
		this.archiveDownloader = archiveDownloader;
		this.profileSetup = profileSetup;
		this.splashImageCopier = splashImageCopier;
		this.shortcutCreator = shortcutCreator;
		this.progressStore = progressStore;
		this.paths = paths;
		this.userSettings = userSettings;
		this.manifestStore = manifestStore;
		this.modlistHashUpdater = modlistHashUpdater;
		this.modlistSetupUpdater = modlistSetupUpdater;
	}

	public void startInstallation(ModlistConfig config, boolean fullInstall, String resumeFromId) {
		isInstalling.set(true);
		Thread thread = new Thread(() -> {
			try {
				runInstallation(config, fullInstall, resumeFromId);
			} catch (Exception e) {
				fileLogger.error("Installation failed", e);
				uiLogger.error(localizer.translate("ERR_INSTALLATION_FAILED", e.toString()));
			} finally {
				isInstalling.set(false);
			}
		}, "install-thread");
		thread.start();
	}

	public boolean isInstalling() {
		return isInstalling.get();
	}

	private void runInstallation(ModlistConfig config, boolean fullInstall, String resumeFromId) throws Exception {
		currentProgressListener.onProgressUpdate(0, 1);
		totalProgressListener.onProgressUpdate(0, 1);

		// NOTE: "+ 1" to count in the separator installation, profile setup and workspace cleanup
		int totalMods = config.getMods().size() + config.getPatches().size() + 1;
		AtomicInteger completedMods = new AtomicInteger(0);
		AtomicBoolean resumePointFound = new AtomicBoolean(resumeFromId == null);
		List<HashMismatch> hashMismatches = new ArrayList<>();
		List<SetupFailure> setupFailures = new ArrayList<>();

		uiLogger.info("\n=================================================================");
		uiLogger.info(localizer.translate("MSG_STARTING_INSTALLATION"));
		uiLogger.info("=================================================================");
		installEntries(config.getMods(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound, hashMismatches, setupFailures);
		installEntries(config.getPatches(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound, hashMismatches, setupFailures);
		installEntries(config.getSeparators(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound, hashMismatches, setupFailures);

		uiLogger.info("\n=================================================================");
		uiLogger.info(localizer.translate("MSG_INSTALLATION_MO2_SETUP"));
		uiLogger.info("=================================================================");
		setupMo2Environment(config);
		totalProgressListener.onProgressUpdate(completedMods.incrementAndGet(), totalMods);

		uiLogger.info("\n=================================================================");
		uiLogger.info(localizer.translate("MSG_INSTALLATION_CLEANUP"));
		uiLogger.info("=================================================================");
		progressStore.clear();
		FileUtils.deleteQuietly(paths.getTempDir().toFile());

		if (!hashMismatches.isEmpty()) {
			presentHashMismatchSummary(hashMismatches);
		}

		if (!setupFailures.isEmpty()) {
			presentSetupFailureSummary(setupFailures);
		}

		uiLogger.info(localizer.translate("MSG_COMPLETE_INSTALLATION"));
		fileLogger.info("Installation completed successfully");

		currentProgressListener.onProgressUpdate(1, 1);
	}

	private void installEntries(List<? extends ModEntry> entries, boolean fullInstall,
	                            int totalMods, AtomicInteger completedMods,
	                            String resumeFromId, AtomicBoolean resumePointFound,
	                            List<HashMismatch> hashMismatches, List<SetupFailure> setupFailures) throws Exception {
		if (entries == null || entries.isEmpty()) {
			return;
		}

		for (ModEntry entry : entries) {
			if (!resumePointFound.get()) {
				if (!entry.getId().equals(resumeFromId)) {
					if (entry.getType() != EntryType.SEPARATOR) {
						completedMods.incrementAndGet();
					}
					continue;
				}
				resumePointFound.set(true);
			}
			progressStore.save(entry.getId());

			uiLogger.info(localizer.translate("MSG_TITLE_CONFIGENTRY", entry.getName()));
			fileLogger.info("Installing entry: %s", entry.getId());

			File archive;
			if (entry instanceof Mod mod) {
				ManifestEntry previousEntry = manifestStore.getManifest().getEntry(mod.getId());

				DownloadResult result = archiveDownloader.download(mod.getUrl(), mod.getId(), mod.getHash(),
						paths.getDownloadsDir().toFile(), fullInstall, currentProgressListener);
				archive = result.archive();

				String installedHash = previousEntry != null ? previousEntry.installedHash() : null;
				if (!fullInstall && installedHash != null && installedHash.equalsIgnoreCase(result.computedHash())) {
					uiLogger.info(localizer.translate("MSG_ADDON_ALREADY_UP_TO_DATE"));
					fileLogger.info("Installed hash matches archive, skipping reinstall: %s", mod.getId());
					if (entry.getType() != EntryType.SEPARATOR) {
						totalProgressListener.onProgressUpdate(completedMods.incrementAndGet(), totalMods);
					}
					continue;
				}

				if (mod.hasHash()) {
					if (!mod.getHash().equalsIgnoreCase(result.computedHash())) {
						hashMismatches.add(new HashMismatch(mod.getName(), mod.getId(), mod.getHash(), result.computedHash()));
						fileLogger.warn("Hash mismatch for %s: expected=%s actual=%s",
								mod.getName(), mod.getHash(), result.computedHash());
					} else {
						fileLogger.info("Hash OK for %s", mod.getName());
					}
				}

				try {
					findInstaller(entry).install(entry, archive, currentProgressListener);
				} catch (SetupPathMissingException e) {
					setupFailures.add(new SetupFailure(mod.getName(), mod.getId(), e.getMissingPaths()));
					fileLogger.warn("Setup path(s) missing for %s: %s", mod.getName(), e.getMissingPaths());
				}

				manifestStore.recordInstall(mod.getId(), result.computedHash());
				applyArchiveCleanup(result, previousEntry);
			} else {
				findInstaller(entry).install(entry, null, currentProgressListener);
			}

			if (entry.getType() != EntryType.SEPARATOR) {
				totalProgressListener.onProgressUpdate(completedMods.incrementAndGet(), totalMods);
			}
		}
	}

	private ModInstaller findInstaller(ModEntry entry) {
		return installers.stream()
				.filter(i -> i.supports(entry))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No installer for entry type: " + entry.getType()));
	}

	private void applyArchiveCleanup(DownloadResult result, ManifestEntry previousEntry) {
		ArchiveCleanupStrategy strategy = userSettings.getArchiveCleanupStrategy();
		File currentArchive = result.archive();

		switch (strategy) {
			case DELETE_ALL -> {
				FileUtils.deleteQuietly(currentArchive);
				fileLogger.info("DELETE_ALL: removed archive %s", currentArchive.getName());
			}
			case KEEP_LATEST_ONLY -> {
				if (previousEntry != null && !previousEntry.filename().equals(currentArchive.getName())) {
					File oldArchive = new File(currentArchive.getParentFile(), previousEntry.filename());
					if (oldArchive.isFile()) {
						FileUtils.deleteQuietly(oldArchive);
						fileLogger.info("KEEP_LATEST_ONLY: removed old archive %s", previousEntry.filename());
					}
				}
			}
			case KEEP_ALL -> { /* do nothing */ }
		}
	}

	private void presentHashMismatchSummary(List<HashMismatch> mismatches) {
		StringBuilder sb = new StringBuilder(localizer.translate("MSG_HASH_MISMATCH_SUMMARY"));
		for (HashMismatch m : mismatches) {
			sb.append("\n").append(localizer.translate("MSG_HASH_MISMATCH_ENTRY",
					m.modName(), m.expectedHash(), m.actualHash()));
		}
		String message = sb.toString();
		uiLogger.warn(message);
		fileLogger.warn(message);
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, message,
					localizer.translate("DLG_HASH_MISMATCH_TITLE"), JOptionPane.WARNING_MESSAGE);
			int choice = JOptionPane.showConfirmDialog(null,
					localizer.translate("DLG_HASH_MISMATCH_UPDATE_MESSAGE"),
					localizer.translate("DLG_HASH_MISMATCH_UPDATE_TITLE"),
					JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				Map<String, String> updates = mismatches.stream()
						.collect(Collectors.toMap(HashMismatch::modId, HashMismatch::actualHash));
				new Thread(() -> modlistHashUpdater.updateHashes(paths.getModlistYaml(), updates),
						"modlist-hash-update").start();
			}
		});
	}

	private void presentSetupFailureSummary(List<SetupFailure> failures) {
		StringBuilder sb = new StringBuilder(localizer.translate("MSG_SETUP_FAILURE_SUMMARY"));
		for (SetupFailure f : failures) {
			sb.append("\n  ").append(f.modName()).append(":");
			for (String path : f.invalidPaths()) {
				sb.append("\n    ").append(path);
			}
		}
		String logMessage = sb.toString();
		uiLogger.warn(logMessage);
		fileLogger.warn(logMessage);

		SwingUtilities.invokeLater(() -> {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			JLabel description = new JLabel(localizer.translate("DLG_SETUP_FAILURE_DESCRIPTION"));
			description.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(description);

			Map<String, Map<String, JTextField>> fieldMap = new LinkedHashMap<>();
			for (SetupFailure f : failures) {
				panel.add(Box.createVerticalStrut(10));

				JLabel modHeader = new JLabel(f.modName());
				modHeader.setFont(modHeader.getFont().deriveFont(Font.BOLD));
				modHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(modHeader);

				JPanel pathsPanel = new JPanel(new GridLayout(0, 2, 4, 2));
				pathsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				fieldMap.put(f.modId(), new LinkedHashMap<>());
				for (String path : f.invalidPaths()) {
					pathsPanel.add(new JLabel(path + ":"));
					JTextField field = new JTextField(path, 40);
					fieldMap.get(f.modId()).put(path, field);
					pathsPanel.add(field);
				}
				panel.add(pathsPanel);
			}

			while (true) {
				int choice = JOptionPane.showConfirmDialog(null, new JScrollPane(panel),
						localizer.translate("DLG_SETUP_FAILURE_TITLE"),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice != JOptionPane.OK_OPTION) {
					break;
				}

				List<String> blankFields = fieldMap.values().stream()
						.flatMap(m -> m.entrySet().stream())
						.filter(e -> e.getValue().getText().trim().isEmpty())
						.map(Map.Entry::getKey)
						.toList();

				if (!blankFields.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							localizer.translate("ERR_SETUP_PATH_BLANK", String.join(", ", blankFields)),
							localizer.translate("DLG_SETUP_FAILURE_TITLE"), JOptionPane.ERROR_MESSAGE);
					continue;
				}

				Map<String, Map<String, String>> corrections = new LinkedHashMap<>();
				fieldMap.forEach((modId, pathFields) -> {
					Map<String, String> modCorrections = new LinkedHashMap<>();
					pathFields.forEach((oldPath, field) -> modCorrections.put(oldPath, field.getText().trim()));
					corrections.put(modId, modCorrections);
				});
				new Thread(() -> modlistSetupUpdater.updateSetup(paths.getModlistYaml(), corrections),
						"modlist-setup-update").start();
				break;
			}
		});
	}

	private void setupMo2Environment(ModlistConfig config) {
		uiLogger.info(localizer.translate("MSG_CREATE_CUSTOM_PROFILE"));
		fileLogger.info("Setting up MO2 profile");
		profileSetup.setupProfile(config.getProfileName());

		uiLogger.info(localizer.translate("MSG_COPY", "splash.png"));
		fileLogger.info("Copying splash image");
		splashImageCopier.copySplashImage();

		uiLogger.info(localizer.translate("MSG_CREATE_SHORTCUT"));
		fileLogger.info("Creating desktop shortcut");
		shortcutCreator.createShortcut(config.getShortcutName());
	}
}

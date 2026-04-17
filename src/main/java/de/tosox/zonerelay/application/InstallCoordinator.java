package de.tosox.zonerelay.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.domain.model.EntryType;
import de.tosox.zonerelay.domain.model.Mod;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.model.ModlistConfig;
import de.tosox.zonerelay.domain.port.*;
import de.tosox.zonerelay.shared.i18n.Localizer;
import de.tosox.zonerelay.shared.logging.LogManager;
import de.tosox.zonerelay.shared.config.AppPaths;
import de.tosox.zonerelay.shared.progress.ProgressListener;
import lombok.Setter;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class InstallCoordinator {
	private final LogManager logManager;
	private final Localizer localizer;
	private final List<ModInstaller> installers;
	private final ArchiveDownloader archiveDownloader;
	private final ProfileSetup profileSetup;
	private final SplashImageCopier splashImageCopier;
	private final ShortcutCreator shortcutCreator;
	private final InstallProgressStore progressStore;
	private final AppPaths paths;

	private final AtomicBoolean isInstalling = new AtomicBoolean(false);

	@Setter
	private ProgressListener currentProgressListener;

	@Setter
	private ProgressListener totalProgressListener;

	@Inject
	public InstallCoordinator(LogManager logManager, Localizer localizer, List<ModInstaller> installers,
	                          ArchiveDownloader archiveDownloader, ProfileSetup profileSetup,
	                          SplashImageCopier splashImageCopier, ShortcutCreator shortcutCreator,
	                          InstallProgressStore progressStore, AppPaths paths) {
		this.logManager = logManager;
		this.localizer = localizer;
		this.installers = installers;
		this.archiveDownloader = archiveDownloader;
		this.profileSetup = profileSetup;
		this.splashImageCopier = splashImageCopier;
		this.shortcutCreator = shortcutCreator;
		this.progressStore = progressStore;
		this.paths = paths;
	}

	public void startInstallation(ModlistConfig config, boolean fullInstall, String resumeFromId) {
		Thread thread = new Thread(() -> {
			isInstalling.set(true);
			try {
				runInstallation(config, fullInstall, resumeFromId);
			} catch (Exception e) {
				logManager.getFileLogger().error("Installation failed: %s", e.getMessage());
				throw new RuntimeException(e);
			} finally {
				isInstalling.set(false);
			}
		});
		thread.start();
	}

	public boolean isInstalling() {
		return isInstalling.get();
	}

	private void runInstallation(ModlistConfig config, boolean fullInstall, String resumeFromId) throws Exception {
		currentProgressListener.onProgressUpdate(0, 1);
		totalProgressListener.onProgressUpdate(0, 1);

		int totalMods = config.getMods().size() + config.getPatches().size() + 1;
		AtomicInteger completedMods = new AtomicInteger(0);
		AtomicBoolean resumePointFound = new AtomicBoolean(resumeFromId == null);

		logManager.getUiLogger().info("\n=================================================================");
		logManager.getUiLogger().info(localizer.translate("MSG_STARTING_INSTALLATION"));
		logManager.getUiLogger().info("=================================================================");
		installEntries(config.getMods(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound);
		installEntries(config.getPatches(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound);
		installEntries(config.getSeparators(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound);

		logManager.getUiLogger().info("\n=================================================================");
		logManager.getUiLogger().info(localizer.translate("MSG_INSTALLATION_MO2_SETUP"));
		logManager.getUiLogger().info("=================================================================");
		setupMo2Environment(config);

		logManager.getUiLogger().info("\n=================================================================");
		logManager.getUiLogger().info(localizer.translate("MSG_INSTALLATION_CLEANUP"));
		logManager.getUiLogger().info("=================================================================");
		progressStore.clear();

		logManager.getUiLogger().info(localizer.translate("MSG_COMPLETE_INSTALLATION"));
		logManager.getFileLogger().info("Installation completed successfully");

		currentProgressListener.onProgressUpdate(1, 1);
		totalProgressListener.onProgressUpdate(1, 1);
	}

	private void installEntries(List<? extends ModEntry> entries, boolean fullInstall,
	                            int totalMods, AtomicInteger completedMods,
	                            String resumeFromId, AtomicBoolean resumePointFound) throws Exception {
		if (entries == null || entries.isEmpty()) {
			return;
		}

		entries.sort(Comparator.comparingInt((ModEntry e) -> switch (e.getType()) {
			case MOD -> 0;
			case PATCH -> 1;
			case SEPARATOR -> 2;
		}));

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

			logManager.getUiLogger().info(localizer.translate("MSG_TITLE_CONFIGENTRY", entry.getName()));
			logManager.getFileLogger().info("Installing entry: {0}", entry.getId());

			File archive = null;
			if (entry instanceof Mod mod) {
				logManager.getUiLogger().info(localizer.translate("MSG_DOWNLOADING_ARCHIVE"));
				archive = archiveDownloader.download(mod.getUrl(), paths.getDownloadsDir().toFile(), currentProgressListener);
			}

			ModInstaller installer = installers.stream()
					.filter(i -> i.supports(entry))
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("No installer for entry type: " + entry.getType()));
			installer.install(entry, archive, currentProgressListener);

			if (entry.getType() != EntryType.SEPARATOR) {
				totalProgressListener.onProgressUpdate(completedMods.incrementAndGet(), totalMods);
			}
		}
	}

	private void setupMo2Environment(ModlistConfig config) {
		logManager.getUiLogger().info(localizer.translate("MSG_CREATE_CUSTOM_PROFILE"));
		logManager.getFileLogger().info("Setting up MO2 profile");
		profileSetup.setupProfile(config.getProfileName());

		logManager.getUiLogger().info(localizer.translate("MSG_COPY", "modlist.txt"));
		logManager.getFileLogger().info("Copying modlist.txt to profile");

		logManager.getUiLogger().info(localizer.translate("MSG_COPY", "splash.png"));
		logManager.getFileLogger().info("Copying splash image");
		splashImageCopier.copySplashImage();

		logManager.getUiLogger().info(localizer.translate("MSG_CREATE_SHORTCUT"));
		logManager.getFileLogger().info("Creating desktop shortcut");
		shortcutCreator.createShortcut(config.getShortcutName());
	}
}

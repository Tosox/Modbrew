package de.tosox.zonerelay.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.model.EntryType;
import de.tosox.zonerelay.domain.model.Mod;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.model.ModlistConfig;
import de.tosox.zonerelay.domain.port.*;
import de.tosox.zonerelay.shared.config.AppPaths;
import de.tosox.zonerelay.shared.i18n.Localizer;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.progress.ProgressListener;
import lombok.Setter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
	                          InstallProgressStore progressStore, AppPaths paths) {
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
	}

	public void startInstallation(ModlistConfig config, boolean fullInstall, String resumeFromId) {
		isInstalling.set(true);
		Thread thread = new Thread(() -> {
			try {
				runInstallation(config, fullInstall, resumeFromId);
			} catch (Exception e) {
				fileLogger.error("Installation failed: %s", e.getMessage());
				uiLogger.error(localizer.translate("ERR_INSTALLATION_FAILED", e.getMessage()));
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

		// NOTE: "+ 1" to count in the separator installation, profile setup and workspace cleanup
		int totalMods = config.getMods().size() + config.getPatches().size() + 1;
		AtomicInteger completedMods = new AtomicInteger(0);
		AtomicBoolean resumePointFound = new AtomicBoolean(resumeFromId == null);

		uiLogger.info("\n=================================================================");
		uiLogger.info(localizer.translate("MSG_STARTING_INSTALLATION"));
		uiLogger.info("=================================================================");
		installEntries(config.getMods(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound);
		installEntries(config.getPatches(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound);
		installEntries(config.getSeparators(), fullInstall, totalMods, completedMods, resumeFromId, resumePointFound);

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

		uiLogger.info(localizer.translate("MSG_COMPLETE_INSTALLATION"));
		fileLogger.info("Installation completed successfully");

		currentProgressListener.onProgressUpdate(1, 1);
	}

	private void installEntries(List<? extends ModEntry> entries, boolean fullInstall,
	                            int totalMods, AtomicInteger completedMods,
	                            String resumeFromId, AtomicBoolean resumePointFound) throws Exception {
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

			File archive = null;
			if (entry instanceof Mod mod) {
				uiLogger.info(localizer.translate("MSG_DOWNLOADING_ARCHIVE"));
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

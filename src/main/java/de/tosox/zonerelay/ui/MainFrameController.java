package de.tosox.zonerelay.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.application.InstallCoordinator;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.model.ModlistConfig;
import de.tosox.zonerelay.domain.port.InstallProgressStore;
import de.tosox.zonerelay.domain.port.ModlistRepository;
import de.tosox.zonerelay.domain.service.ModlistValidator;
import de.tosox.zonerelay.shared.i18n.Localizer;
import com.google.inject.name.Named;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.config.AppPaths;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class MainFrameController {
	private final Localizer localizer;
	private final Logger fileLogger;
	private final Logger uiLogger;
	private final MainFrame mainFrame;
	private final InstallCoordinator installCoordinator;
	private final InstallProgressStore progressStore;
	private final AppPaths paths;
	private final ModlistRepository modlistRepository;
	private final ModlistValidator modlistValidator;

	@Inject
	public MainFrameController(Localizer localizer, @Named("file") Logger fileLogger,
	                           @Named("ui") Logger uiLogger, MainFrame mainFrame,
	                           InstallCoordinator installCoordinator, InstallProgressStore progressStore,
	                           AppPaths paths, ModlistRepository modlistRepository,
	                           ModlistValidator modlistValidator) {
		this.localizer = localizer;
		this.fileLogger = fileLogger;
		this.uiLogger = uiLogger;
		this.mainFrame = mainFrame;
		this.installCoordinator = installCoordinator;
		this.progressStore = progressStore;
		this.paths = paths;
		this.modlistRepository = modlistRepository;
		this.modlistValidator = modlistValidator;

		installCoordinator.setCurrentProgressListener((current, total) -> {
			if (total <= 0) {
				mainFrame.setCurrentProgressIndeterminate();
			} else {
				mainFrame.setCurrentProgress((int) (current * 100 / total));
			}
		});
		installCoordinator.setTotalProgressListener((current, total) ->
				mainFrame.setTotalProgress(total <= 0 ? 0 : (int) (current * 100 / total))
		);
	}

	public void onInstallClick() {
		if (installCoordinator.isInstalling()) {
			uiLogger.warn(localizer.translate("ERR_ALREADY_INSTALLING"));
			fileLogger.warn("Installation already in progress");
			return;
		}

		if (Files.notExists(paths.getMo2Exe())) {
			uiLogger.warn(localizer.translate("ERR_INVALID_INSTALL_DIR"));
			fileLogger.warn("Please move the installer into the MO2 directory");
			return;
		}

		if (Files.notExists(paths.getMo2Config())) {
			uiLogger.warn(localizer.translate("ERR_LAUNCH_MO2"));
			fileLogger.warn("Please launch MO2 once first");
			return;
		}

		uiLogger.info(localizer.translate("MSG_READ_MODLIST_CFG"));
		fileLogger.info("Reading modlist configuration");

		ModlistConfig config;
		try {
			config = modlistRepository.load(paths.getModlistYaml());
			modlistValidator.validate(config);
		} catch (Exception e) {
			uiLogger.error(localizer.translate("ERR_CONFIG_INVALID", e.getMessage()));
			fileLogger.error("Failed to load or validate config: " + e.getMessage());
			return;
		}

		String resumeFromId = promptForResume(config);

		installCoordinator.startInstallation(config, mainFrame.isFullInstallSelected(), resumeFromId);
	}

	public void onLaunchClick() {
		if (Files.notExists(paths.getMo2Exe())) {
			uiLogger.warn(localizer.translate("ERR_INVALID_INSTALL_DIR"));
			fileLogger.warn("Please move the installer into the MO2 directory");
			return;
		}

		try {
			new ProcessBuilder(paths.getMo2Exe().toString())
					.directory(paths.getMo2Dir().toFile())
					.start();
		} catch (IOException e) {
			uiLogger.error(localizer.translate("ERR_LAUNCH_MO2_FAIL"));
			fileLogger.error("Failed to launch MO2: " + e.getMessage());
		}
	}

	private String promptForResume(ModlistConfig config) {
		if (!progressStore.hasSavedState()) {
			return null;
		}

		String savedId = progressStore.load();
		if (savedId == null) {
			return null;
		}

		String entryName = findEntryNameById(config, savedId);
		String displayName = (entryName != null) ? entryName : savedId;

		int choice = JOptionPane.showConfirmDialog(
				mainFrame,
				localizer.translate("DLG_RESUME_MESSAGE", displayName),
				localizer.translate("DLG_RESUME_TITLE"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE
		);

		if (choice == JOptionPane.YES_OPTION) {
			return savedId;
		}

		progressStore.clear();
		return null;
	}

	private String findEntryNameById(ModlistConfig config, String id) {
		return Stream.of(config.getMods(), config.getPatches(), config.getSeparators())
				.flatMap(List::stream)
				.filter(entry -> entry.getId().equals(id))
				.map(ModEntry::getName)
				.findFirst()
				.orElse(null);
	}
}

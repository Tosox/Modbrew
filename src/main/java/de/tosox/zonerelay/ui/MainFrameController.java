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
import de.tosox.zonerelay.shared.logging.LogManager;
import de.tosox.zonerelay.shared.config.AppPaths;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class MainFrameController {
	private final Localizer localizer;
	private final LogManager logManager;
	private final MainFrame mainFrame;
	private final InstallCoordinator installCoordinator;
	private final InstallProgressStore progressStore;
	private final AppPaths paths;
	private final ModlistRepository modlistRepository;
	private final ModlistValidator modlistValidator;

	@Inject
	public MainFrameController(Localizer localizer, LogManager logManager,
	                           MainFrame mainFrame, InstallCoordinator installCoordinator,
	                           InstallProgressStore progressStore, AppPaths paths,
	                           ModlistRepository modlistRepository, ModlistValidator modlistValidator) {
		this.localizer = localizer;
		this.logManager = logManager;
		this.mainFrame = mainFrame;
		this.installCoordinator = installCoordinator;
		this.progressStore = progressStore;
		this.paths = paths;
		this.modlistRepository = modlistRepository;
		this.modlistValidator = modlistValidator;
	}

	public void onInstallClick() {
		if (installCoordinator.isInstalling()) {
			logManager.getUiLogger().warn(localizer.translate("ERR_ALREADY_INSTALLING"));
			logManager.getFileLogger().warn("Installation already in progress");
			return;
		}

		if (Files.notExists(paths.mo2Exe)) {
			logManager.getUiLogger().warn(localizer.translate("ERR_INVALID_INSTALL_DIR"));
			logManager.getFileLogger().warn("Please move the installer into the MO2 directory");
			return;
		}

		if (Files.notExists(paths.mo2Config)) {
			logManager.getUiLogger().warn(localizer.translate("ERR_LAUNCH_MO2"));
			logManager.getFileLogger().warn("Please launch MO2 once first");
			return;
		}

		logManager.getUiLogger().info(localizer.translate("MSG_READ_MODLIST_CFG"));
		logManager.getFileLogger().info("Reading modlist configuration");

		ModlistConfig config;
		try {
			config = modlistRepository.load(paths.modlistYaml);
			modlistValidator.validate(config);
		} catch (Exception e) {
			logManager.getUiLogger().error(localizer.translate("ERR_CONFIG_INVALID"));
			logManager.getFileLogger().error("Failed to load or validate config: " + e.getMessage());
			return;
		}

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

		String resumeFromId = promptForResume(config);

		try {
			installCoordinator.startInstallation(config, mainFrame.isFullInstallSelected(), resumeFromId);
		} catch (Exception e) {
			logManager.getUiLogger().error(localizer.translate("ERR_INSTALLATION_FAILED"));
			logManager.getFileLogger().error("Installation failed: " + e.getMessage());
		}
	}

	public void onLaunchClick() {
		if (Files.notExists(paths.mo2Exe)) {
			logManager.getUiLogger().warn(localizer.translate("ERR_INVALID_INSTALL_DIR"));
			logManager.getFileLogger().warn("Please move the installer into the MO2 directory");
			return;
		}

		try {
			Runtime.getRuntime().exec(paths.mo2Exe.toString(), null, paths.mo2Dir.toFile());
		} catch (IOException e) {
			logManager.getUiLogger().error(localizer.translate("ERR_LAUNCH_MO2_FAIL"));
			logManager.getFileLogger().error("Failed to launch MO2: " + e.getMessage());
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

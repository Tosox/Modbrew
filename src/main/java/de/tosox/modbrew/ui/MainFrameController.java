package de.tosox.modbrew.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.modbrew.application.InstallCoordinator;
import de.tosox.modbrew.domain.model.ModEntry;
import de.tosox.modbrew.domain.model.BrewfileConfig;
import de.tosox.modbrew.domain.port.BrewfileRepository;
import de.tosox.modbrew.domain.port.InstallProgressStore;
import de.tosox.modbrew.domain.service.BrewfileValidator;
import de.tosox.modbrew.shared.i18n.Localizer;
import com.google.inject.name.Named;
import de.tosox.modbrew.shared.logging.Logger;
import de.tosox.modbrew.shared.config.AppPaths;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
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
	private final BrewfileRepository brewfileRepository;
	private final BrewfileValidator brewfileValidator;

	@Inject
	public MainFrameController(Localizer localizer, @Named("file") Logger fileLogger,
	                           @Named("ui") Logger uiLogger, MainFrame mainFrame,
	                           InstallCoordinator installCoordinator, InstallProgressStore progressStore,
	                           AppPaths paths, BrewfileRepository brewfileRepository,
	                           BrewfileValidator brewfileValidator) {
		this.localizer = localizer;
		this.fileLogger = fileLogger;
		this.uiLogger = uiLogger;
		this.mainFrame = mainFrame;
		this.installCoordinator = installCoordinator;
		this.progressStore = progressStore;
		this.paths = paths;
		this.brewfileRepository = brewfileRepository;
		this.brewfileValidator = brewfileValidator;

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
			uiLogger.warn(localizer.translate("ERR_ALREADY_BREWING"));
			fileLogger.warn("Brew already in progress");
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

		uiLogger.info(localizer.translate("MSG_READ_BREWFILE_CFG"));
		fileLogger.info("Reading brewfile configuration");

		BrewfileConfig config;
		try {
			config = brewfileRepository.load(paths.getBrewfileYaml());
			brewfileValidator.validate(config);
		} catch (Exception e) {
			uiLogger.error(localizer.translate("ERR_CONFIG_INVALID"));
			fileLogger.error("Failed to load or validate config", e);
			return;
		}

		Optional<String> resumeResult = promptForResume(config);
		if (resumeResult.isEmpty()) {
			return;
		}

		String resumeFromId = resumeResult.get().isEmpty() ? null : resumeResult.get();
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

	private Optional<String> promptForResume(BrewfileConfig config) {
		if (!progressStore.hasSavedState()) {
			return Optional.of("");
		}

		String savedId = progressStore.load();
		if (savedId == null) {
			return Optional.of("");
		}

		String entryName = findEntryNameById(config, savedId);
		if (entryName == null) {
			int choice = JOptionPane.showConfirmDialog(
					mainFrame,
					localizer.translate("DLG_STALE_PROGRESS_MESSAGE"),
					localizer.translate("DLG_STALE_PROGRESS_TITLE"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE
			);
			if (choice != JOptionPane.OK_OPTION) {
				return Optional.empty();
			}
			progressStore.clear();
			return Optional.of("");
		}

		int choice = JOptionPane.showConfirmDialog(
				mainFrame,
				localizer.translate("DLG_RESUME_MESSAGE", entryName),
				localizer.translate("DLG_RESUME_TITLE"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE
		);

		if (choice == JOptionPane.YES_OPTION) {
			return Optional.of(savedId);
		}

		progressStore.clear();
		return Optional.of("");
	}

	private String findEntryNameById(BrewfileConfig config, String id) {
		return Stream.of(config.getMods(), config.getPatches(), config.getSeparators())
				.flatMap(List::stream)
				.filter(entry -> entry.getId().equals(id))
				.map(ModEntry::getName)
				.findFirst()
				.orElse(null);
	}
}

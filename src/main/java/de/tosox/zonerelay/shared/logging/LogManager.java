package de.tosox.zonerelay.shared.logging;

import lombok.Getter;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class LogManager {
	private final Path logFolder;
	private final Logger fileLogger;
	private final Logger uiLogger;

	public LogManager(JTextPane outputPane, Path logsDir) {
		this.logFolder = createRunLogFolder(logsDir);

		// TODO: Don't hardcode LogLevel
		this.fileLogger = new FileLogger(logFolder.resolve("app.log"), LogLevel.INFO);
		this.uiLogger = new UILogger(outputPane);
	}

	private Path createRunLogFolder(Path baseDir) {
		// TODO: Clean up log directories
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String folderName = "log_" + formatter.format(LocalDateTime.now());
		Path folder = baseDir.resolve(folderName);
		try {
			Files.createDirectories(folder);
		} catch (IOException e) {
			throw new RuntimeException("Could not create log folder: " + folder, e);
		}
		return folder;
	}
}

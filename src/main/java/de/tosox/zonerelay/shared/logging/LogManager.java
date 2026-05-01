package de.tosox.zonerelay.shared.logging;

import lombok.Getter;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Getter
public class LogManager {
	private final Path logFolder;
	private final Logger fileLogger;
	private final Logger uiLogger;
	private final int logRetentionCount;

	public LogManager(JTextPane outputPane, Path logsDir, LogLevel logLevel, int logRetentionCount) {
		this.logRetentionCount = logRetentionCount;
		this.logFolder = createRunLogFolder(logsDir);
		this.fileLogger = new FileLogger(logFolder.resolve("app.log"), logLevel);
		this.uiLogger = new UILogger(outputPane);
	}

	private Path createRunLogFolder(Path baseDir) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String folderName = "log_" + formatter.format(LocalDateTime.now());
		Path folder = baseDir.resolve(folderName);
		try {
			Files.createDirectories(folder);
		} catch (IOException e) {
			throw new RuntimeException("Could not create log folder: " + folder, e);
		}
		pruneOldLogFolders(baseDir);
		return folder;
	}

	private void pruneOldLogFolders(Path baseDir) {
		try (var stream = Files.list(baseDir)) {
			List<Path> runs = stream
					.filter(p -> Files.isDirectory(p) && p.getFileName().toString().startsWith("log_"))
					.sorted(Comparator.comparing(p -> p.getFileName().toString()))
					.toList();
			int excess = runs.size() - logRetentionCount;
			for (int i = 0; i < excess; i++) {
				FileUtils.deleteDirectory(runs.get(i).toFile());
			}
		} catch (IOException ignored) {}
	}
}

package de.tosox.modbrew.shared.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements Logger {
	private final PrintWriter writer;
	private final LogLevel minLevel;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public FileLogger(Path logFilePath, LogLevel minLevel) {
		this.minLevel = minLevel;
		try {
			writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true), true);
		} catch (IOException e) {
			throw new RuntimeException("Could not open log file: " + logFilePath, e);
		}
	}

	private boolean shouldLog(LogLevel level) {
		return level.ordinal() >= minLevel.ordinal();
	}

	private void log(String level, String message) {
		String timestamp = formatter.format(LocalDateTime.now());
		writer.printf("[%s][%s] %s%n", timestamp, level, message);
	}

	@Override
	public void debug(String message) {
		if (shouldLog(LogLevel.DEBUG)) {
			log("DEBUG", message);
		}
	}

	@Override
	public void debug(String msg, Object... args) {
		if (shouldLog(LogLevel.DEBUG)) {
			log("DEBUG", String.format(msg, args));
		}
	}

	@Override
	public void info(String message) {
		if (shouldLog(LogLevel.INFO)) {
			log("INFO", message);
		}
	}

	@Override
	public void info(String msg, Object... args) {
		if (shouldLog(LogLevel.INFO)) {
			log("INFO", String.format(msg, args));
		}
	}

	@Override
	public void warn(String message) {
		if (shouldLog(LogLevel.WARN)) {
			log("WARN", message);
		}
	}

	@Override
	public void warn(String msg, Object... args) {
		if (shouldLog(LogLevel.WARN)) {
			log("WARN", String.format(msg, args));
		}
	}

	@Override
	public void error(String message) {
		if (shouldLog(LogLevel.ERROR)) {
			log("ERROR", message);
		}
	}

	@Override
	public void error(String msg, Object... args) {
		if (shouldLog(LogLevel.ERROR)) {
			log("ERROR", String.format(msg, args));
		}
	}

	@Override
	public void error(String message, Exception e) {
		if (shouldLog(LogLevel.ERROR)) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log("ERROR", message + System.lineSeparator() + sw);
		}
	}
}

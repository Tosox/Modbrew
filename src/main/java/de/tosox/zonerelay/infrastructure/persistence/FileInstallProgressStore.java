package de.tosox.zonerelay.infrastructure.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.port.InstallProgressStore;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.config.AppPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class FileInstallProgressStore implements InstallProgressStore {
	private final Logger logger;
	private final Path progressFile;

	@Inject
	public FileInstallProgressStore(@Named("file") Logger logger, AppPaths paths) {
		this.logger = logger;
		this.progressFile = paths.getProgressFile();
	}

	@Override
	public void save(String entryId) {
		try {
			Files.writeString(progressFile, entryId, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Failed to save install progress", e);
		}
	}

	@Override
	public boolean hasSavedState() {
		return Files.exists(progressFile);
	}

	@Override
	public String load() {
		if (!hasSavedState()) {
			return null;
		}

		try {
			return Files.readString(progressFile, StandardCharsets.UTF_8).trim();
		} catch (IOException e) {
			logger.error("Failed to read install progress", e);
		}

		return null;
	}

	@Override
	public void clear() {
		try {
			Files.deleteIfExists(progressFile);
		} catch (IOException e) {
			logger.error("Failed to delete install progress file", e);
		}
	}
}

package de.tosox.zonerelay.infrastructure.extraction;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.port.ArchiveExtractor;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.config.AppPaths;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Singleton
public class SevenZipExtractor implements ArchiveExtractor {
	private final Logger logger;
	private final String sevenZipPath;

	@Inject
	public SevenZipExtractor(@Named("file") Logger logger, AppPaths paths) {
		this.logger = logger;
		this.sevenZipPath = paths.getSevenZipExe().toString();
	}

	@Override
	public void extract(File archive, Path destination) throws Exception {
		logger.info("Extracting %s to %s", archive.getPath(), destination);

		ProcessBuilder builder = new ProcessBuilder(
				sevenZipPath, "-bso0", "x", archive.getPath(), "-o" + destination.toString(), "-y"
		);
		builder.redirectErrorStream(true);
		Process process = builder.start();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				logger.info(line);
			}
		}

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new IOException("Extraction process failed with exit code " + exitCode);
		}
	}
}

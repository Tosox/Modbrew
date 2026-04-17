package de.tosox.zonerelay.infrastructure.mo2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Mo2ConfigReader {
	private final Path mo2ConfigPath;

	public Mo2ConfigReader(Path mo2ConfigPath) {
		this.mo2ConfigPath = mo2ConfigPath;
	}

	public Path getGamePath() throws IOException {
		try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(mo2ConfigPath), StandardCharsets.UTF_8)) {
			Properties properties = new Properties();
			properties.load(reader);

			String gamePath = properties.getProperty("gamePath");
			if (gamePath == null) {
				throw new IOException("gamePath not found in MO2 configuration");
			}

			return Paths.get(gamePath.replace("@ByteArray(", "").replace(")", ""));
		}
	}
}

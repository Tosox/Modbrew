package de.tosox.zonerelay.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import de.tosox.zonerelay.shared.config.UserSettings;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

public class UserSettingsManager {
	private static final ObjectMapper MAPPER = new ObjectMapper(
			YAMLFactory.builder()
					.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
					.build());

	private final File configFile;

	@Getter
	private UserSettings current;

	public UserSettingsManager(File configFile) {
		this.configFile = configFile;
		this.current = load();
	}

	public void save(UserSettings settings) {
		try {
			MAPPER.writerWithDefaultPrettyPrinter().writeValue(configFile, settings);
			current = settings;
		} catch (IOException e) {
			throw new RuntimeException("Failed to save user settings", e);
		}
	}

	private UserSettings load() {
		if (!configFile.exists()) {
			UserSettings defaults = UserSettings.defaults();
			save(defaults);
			return defaults;
		}
		try {
			return MAPPER.readValue(configFile, UserSettings.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load user settings", e);
		}
	}
}

package de.tosox.zonerelay.shared.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tosox.zonerelay.shared.logging.LogLevel;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class UserSettings {
	private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
	private static final String DEFAULT_LANGUAGE = "en-US";
	private static final LogLevel DEFAULT_LOGLEVEL = LogLevel.INFO;

	private final String language;
	private final LogLevel logLevel;

	public UserSettings() {
		this.language = DEFAULT_LANGUAGE;
		this.logLevel = DEFAULT_LOGLEVEL;
	}

	@JsonCreator
	public UserSettings(
			@JsonProperty("language") String language,
			@JsonProperty("logLevel") LogLevel logLevel) {
		this.language = language != null ? language : DEFAULT_LANGUAGE;
		this.logLevel = logLevel != null ? logLevel : DEFAULT_LOGLEVEL;
	}

	public static UserSettings load(File file) {
		if (!file.exists()) {
			UserSettings defaults = new UserSettings();
			defaults.save(file);
			return defaults;
		}

		try {
			return MAPPER.readValue(file, UserSettings.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load user settings", e);
		}
	}

	public void save(File file) {
		try {
			MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, this);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save user settings", e);
		}
	}
}

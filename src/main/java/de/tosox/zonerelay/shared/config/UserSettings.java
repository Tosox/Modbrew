package de.tosox.zonerelay.shared.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.tosox.zonerelay.shared.logging.LogLevel;
import lombok.Getter;
import lombok.With;

@With
@Getter
public class UserSettings {
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

	public static UserSettings defaults() {
		return new UserSettings();
	}
}

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
	private static final ArchiveCleanupStrategy DEFAULT_CLEANUP = ArchiveCleanupStrategy.KEEP_LATEST_ONLY;
	private static final int DEFAULT_LOG_RETENTION_COUNT = 10;

	private final String language;
	private final LogLevel logLevel;
	private final ArchiveCleanupStrategy archiveCleanupStrategy;
	private final int logRetentionCount;

	public UserSettings() {
		this.language = DEFAULT_LANGUAGE;
		this.logLevel = DEFAULT_LOGLEVEL;
		this.archiveCleanupStrategy = DEFAULT_CLEANUP;
		this.logRetentionCount = DEFAULT_LOG_RETENTION_COUNT;
	}

	@JsonCreator
	public UserSettings(
			@JsonProperty("language") String language,
			@JsonProperty("logLevel") LogLevel logLevel,
			@JsonProperty("archiveCleanupStrategy") ArchiveCleanupStrategy archiveCleanupStrategy,
			@JsonProperty("logRetentionCount") Integer logRetentionCount) {
		this.language = language != null ? language : DEFAULT_LANGUAGE;
		this.logLevel = logLevel != null ? logLevel : DEFAULT_LOGLEVEL;
		this.archiveCleanupStrategy = archiveCleanupStrategy != null ? archiveCleanupStrategy : DEFAULT_CLEANUP;
		this.logRetentionCount = logRetentionCount != null ? logRetentionCount : DEFAULT_LOG_RETENTION_COUNT;
	}

	public static UserSettings defaults() {
		return new UserSettings();
	}
}

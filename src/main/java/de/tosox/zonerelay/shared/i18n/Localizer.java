package de.tosox.zonerelay.shared.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tosox.zonerelay.shared.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Localizer {
	private final Map<String, String> messages = new HashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Logger logger;

	public Localizer(String languageCode, Logger logger, Path localesDir) throws IOException {
		this.logger = logger;
		loadLocale(localesDir, languageCode);
	}

	private void loadLocale(Path localeDir, String languageCode) throws IOException {
		Path filePath = localeDir.resolve(languageCode + ".json");
		if (!Files.exists(filePath)) {
			throw new IOException("Localization file not found: " + filePath);
		}

		try {
			Map<String, String> loaded = objectMapper.readValue(filePath.toFile(), new TypeReference<>() {});
			messages.clear();
			messages.putAll(loaded);
		} catch (IOException e) {
			throw new IOException("Failed to load locale file: " + filePath, e);
		}
	}

	public String translate(String key, Object... args) {
		String message = messages.get(key);
		if (message == null) {
			logger.warn("No valid translation for '%s'", key);
			return key;
		}
		try {
			if (args.length == 0) {
				return message;
			}
			return MessageFormat.format(message, args);
		} catch (IllegalArgumentException e) {
			logger.warn("Unable to format string for '%s' with '%s'", key, Arrays.toString(args));
			return key;
		}
	}
}

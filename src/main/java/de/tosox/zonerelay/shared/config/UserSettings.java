package de.tosox.zonerelay.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;

@Getter
@Setter
public class UserSettings {
	private String language = "en-US";

	public static UserSettings load(File file) {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			if (!file.exists()) {
				return new UserSettings();
			}
			return mapper.readValue(file, UserSettings.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load user settings", e);
		}
	}

	public void save(File file) {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(file, this);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save user settings", e);
		}
	}
}

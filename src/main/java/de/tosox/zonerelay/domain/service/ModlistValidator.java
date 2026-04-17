package de.tosox.zonerelay.domain.service;

import de.tosox.zonerelay.domain.model.Mod;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.model.ModlistConfig;
import de.tosox.zonerelay.domain.model.Separator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModlistValidator {
	public void validate(ModlistConfig config) {
		if (config.getProfileName() == null || config.getProfileName().isBlank()) {
			throw new IllegalArgumentException("modlist.yaml is missing 'profileName'");
		}
		if (config.getShortcutName() == null || config.getShortcutName().isBlank()) {
			throw new IllegalArgumentException("modlist.yaml is missing 'shortcutName'");
		}

		Set<String> seenIds = new HashSet<>();

		validateEntries(config.getMods(), seenIds);
		validateEntries(config.getPatches(), seenIds);
		validateEntries(config.getSeparators(), seenIds);
	}

	private void validateEntries(List<? extends ModEntry> entries, Set<String> seenIds) {
		for (ModEntry entry : entries) {
			validateEntry(entry, seenIds);
		}
	}

	private void validateEntry(ModEntry entry, Set<String> seenIds) {
		if (entry.getId() == null || entry.getId().isBlank()) {
			throw new IllegalArgumentException("Entry missing id: " + entry);
		}

		if (!seenIds.add(entry.getId())) {
			throw new IllegalArgumentException("Duplicate id found: " + entry.getId());
		}

		if (entry instanceof Mod mod) {
			if (mod.getName() == null || mod.getName().isBlank()) {
				throw new IllegalArgumentException("Mod missing name: " + mod.getId());
			}
			validateUrl(mod.getUrl(), mod.getId());
		} else if (entry instanceof Separator separator) {
			if (separator.getName() == null || separator.getName().isBlank()) {
				throw new IllegalArgumentException("Separator missing name: " + separator.getId());
			}
		} else {
			throw new IllegalArgumentException("Unknown ModEntry type: " + entry.getClass().getSimpleName());
		}
	}

	private void validateUrl(String urlString, String id) {
		if (urlString == null || urlString.isBlank()) {
			throw new IllegalArgumentException("Entry with id '" + id + "' is missing a URL");
		}

		try {
			//noinspection ResultOfMethodCallIgnored
			new URI(urlString).toURL();
		} catch (URISyntaxException | MalformedURLException e) {
			throw new IllegalArgumentException("Entry with id '" + id + "' has invalid URL: " + urlString);
		}
	}
}

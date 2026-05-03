package de.tosox.modbrew.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.modbrew.shared.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Singleton
public class BrewfileSetupUpdater {
	private static final ObjectMapper MAPPER = new ObjectMapper(
			YAMLFactory.builder()
					.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
					.build());

	private final Logger logger;

	@Inject
	public BrewfileSetupUpdater(@Named("file") Logger logger) {
		this.logger = logger;
	}

	public void updateRecipe(Path brewfilePath, Map<String, Map<String, String>> idToCorrections) {
		try {
			JsonNode root = MAPPER.readTree(brewfilePath.toFile());
			for (String listKey : List.of("mods", "patches")) {
				JsonNode list = root.get(listKey);
				if (list == null) {
					continue;
				}
				for (JsonNode node : list) {
					String id = node.path("id").asText(null);
					if (id == null || !idToCorrections.containsKey(id)) {
						continue;
					}
					Map<String, String> corrections = idToCorrections.get(id);
					ArrayNode recipeNode = (ArrayNode) node.get("recipe");
					if (recipeNode == null) {
						continue;
					}
					ArrayNode updated = MAPPER.createArrayNode();
					for (JsonNode entry : recipeNode) {
						String path = entry.asText();
						updated.add(corrections.getOrDefault(path, path));
					}
					((ObjectNode) node).set("recipe", updated);
				}
			}
			MAPPER.writerWithDefaultPrettyPrinter().writeValue(brewfilePath.toFile(), root);
			logger.info("Updated recipe paths for %d mods in modbrew.yaml", idToCorrections.size());
		} catch (IOException e) {
			logger.error("Failed to update modbrew.yaml recipe paths: %s", e.getMessage());
		}
	}
}

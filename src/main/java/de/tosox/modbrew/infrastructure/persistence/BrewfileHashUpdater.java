package de.tosox.modbrew.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class BrewfileHashUpdater {
	private static final ObjectMapper MAPPER = new ObjectMapper(
			YAMLFactory.builder()
					.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
					.build());

	private final Logger logger;

	@Inject
	public BrewfileHashUpdater(@Named("file") Logger logger) {
		this.logger = logger;
	}

	public void updateHashes(Path brewfilePath, Map<String, String> idToHash) {
		try {
			JsonNode root = MAPPER.readTree(brewfilePath.toFile());
			for (String listKey : List.of("mods", "patches")) {
				JsonNode list = root.get(listKey);
				if (list == null) {
					continue;
				}
				for (JsonNode node : list) {
					String id = node.path("id").asText(null);
					if (id != null && idToHash.containsKey(id)) {
						((ObjectNode) node).put("hash", idToHash.get(id));
					}
				}
			}
			MAPPER.writerWithDefaultPrettyPrinter().writeValue(brewfilePath.toFile(), root);
			logger.info("Updated %d hashes in modbrew.yaml", idToHash.size());
		} catch (IOException e) {
			logger.error("Failed to update modbrew.yaml hashes: %s", e.getMessage());
		}
	}
}

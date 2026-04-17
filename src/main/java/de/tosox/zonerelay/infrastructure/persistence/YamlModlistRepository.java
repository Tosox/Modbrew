package de.tosox.zonerelay.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Singleton;
import de.tosox.zonerelay.domain.model.ModlistConfig;
import de.tosox.zonerelay.domain.port.ModlistRepository;

import java.nio.file.Path;

@Singleton
public class YamlModlistRepository implements ModlistRepository {
	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	@Override
	public ModlistConfig load(Path path) throws Exception {
		return objectMapper.readValue(path.toFile(), ModlistConfig.class);
	}
}

package de.tosox.modbrew.infrastructure.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Singleton;
import de.tosox.modbrew.domain.model.BrewfileConfig;
import de.tosox.modbrew.domain.port.BrewfileRepository;

import java.nio.file.Path;

@Singleton
public class YamlBrewfileRepository implements BrewfileRepository {
	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	@Override
	public BrewfileConfig load(Path path) throws Exception {
		return objectMapper.readValue(path.toFile(), BrewfileConfig.class);
	}
}

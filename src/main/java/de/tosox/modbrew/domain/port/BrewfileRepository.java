package de.tosox.modbrew.domain.port;

import de.tosox.modbrew.domain.model.BrewfileConfig;

import java.nio.file.Path;

public interface BrewfileRepository {
	BrewfileConfig load(Path path) throws Exception;
}

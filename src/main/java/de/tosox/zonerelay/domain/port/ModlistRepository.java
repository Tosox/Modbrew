package de.tosox.zonerelay.domain.port;

import de.tosox.zonerelay.domain.model.ModlistConfig;

import java.nio.file.Path;

public interface ModlistRepository {
	ModlistConfig load(Path path) throws Exception;
}

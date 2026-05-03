package de.tosox.modbrew.domain.port;

import de.tosox.modbrew.domain.model.ModEntry;

import java.io.IOException;
import java.nio.file.Path;

public interface MetaIniWriter {
	void generate(ModEntry entry, Path targetDir) throws IOException;
}

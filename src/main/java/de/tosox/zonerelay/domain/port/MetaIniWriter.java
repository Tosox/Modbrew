package de.tosox.zonerelay.domain.port;

import de.tosox.zonerelay.domain.model.ModEntry;

import java.io.IOException;
import java.nio.file.Path;

public interface MetaIniWriter {
	void generate(ModEntry entry, Path targetDir) throws IOException;
}

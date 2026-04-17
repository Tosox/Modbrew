package de.tosox.zonerelay.domain.port;

import java.io.File;
import java.nio.file.Path;

public interface ArchiveExtractor {
	void extract(File archive, Path destination) throws Exception;
}

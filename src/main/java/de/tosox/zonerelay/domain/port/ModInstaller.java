package de.tosox.zonerelay.domain.port;

import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.shared.progress.ProgressListener;

import java.io.File;

public interface ModInstaller {
	boolean supports(ModEntry entry);
	void install(ModEntry entry, File archive, ProgressListener progressListener) throws Exception;
}

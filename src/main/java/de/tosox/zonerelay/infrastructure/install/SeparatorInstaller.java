package de.tosox.zonerelay.infrastructure.install;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.domain.model.EntryType;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.model.Separator;
import de.tosox.zonerelay.domain.port.MetaIniWriter;
import de.tosox.zonerelay.domain.port.ModInstaller;
import de.tosox.zonerelay.shared.i18n.Localizer;
import de.tosox.zonerelay.shared.logging.LogManager;
import de.tosox.zonerelay.shared.config.AppPaths;
import de.tosox.zonerelay.shared.progress.ProgressListener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class SeparatorInstaller implements ModInstaller {
	private final LogManager logManager;
	private final Localizer localizer;
	private final MetaIniWriter metaIniWriter;
	private final AppPaths paths;

	@Inject
	public SeparatorInstaller(LogManager logManager, Localizer localizer,
	                          MetaIniWriter metaIniWriter, AppPaths paths) {
		this.logManager = logManager;
		this.localizer = localizer;
		this.metaIniWriter = metaIniWriter;
		this.paths = paths;
	}

	@Override
	public boolean supports(ModEntry entry) {
		return entry.getType() == EntryType.SEPARATOR;
	}

	@Override
	public void install(ModEntry entry, File archive, ProgressListener progressListener) throws Exception {
		if (!(entry instanceof Separator separator)) {
			throw new IllegalArgumentException("Expected Separator, got " + entry.getClass().getSimpleName());
		}
		progressListener.onProgressUpdate(0, 1);

		Path modDir = paths.getModsDir().resolve(separator.getName() + "_separator");

		logManager.getUiLogger().info(localizer.translate("MSG_CREATE_SEPARATOR", modDir));
		logManager.getFileLogger().info("Creating separator: %s", separator.getName());
		Files.createDirectories(modDir);

		metaIniWriter.generate(separator, modDir);
		progressListener.onProgressUpdate(1, 1);
	}
}

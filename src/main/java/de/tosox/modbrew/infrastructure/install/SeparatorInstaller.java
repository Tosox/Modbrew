package de.tosox.modbrew.infrastructure.install;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.modbrew.domain.model.EntryType;
import de.tosox.modbrew.domain.model.ModEntry;
import de.tosox.modbrew.domain.model.Separator;
import de.tosox.modbrew.domain.port.MetaIniWriter;
import de.tosox.modbrew.domain.port.ModInstaller;
import de.tosox.modbrew.shared.i18n.Localizer;
import com.google.inject.name.Named;
import de.tosox.modbrew.shared.logging.Logger;
import de.tosox.modbrew.shared.config.AppPaths;
import de.tosox.modbrew.shared.progress.ProgressListener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class SeparatorInstaller implements ModInstaller {
	private final Logger fileLogger;
	private final Logger uiLogger;
	private final Localizer localizer;
	private final MetaIniWriter metaIniWriter;
	private final AppPaths paths;

	@Inject
	public SeparatorInstaller(@Named("file") Logger fileLogger, @Named("ui") Logger uiLogger,
	                          Localizer localizer, MetaIniWriter metaIniWriter, AppPaths paths) {
		this.fileLogger = fileLogger;
		this.uiLogger = uiLogger;
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

		uiLogger.info(localizer.translate("MSG_CREATE_SEPARATOR", paths.relativize(modDir)));
		fileLogger.info("Creating separator: %s", separator.getName());
		Files.createDirectories(modDir);

		metaIniWriter.generate(separator, modDir);
		progressListener.onProgressUpdate(1, 1);
	}
}

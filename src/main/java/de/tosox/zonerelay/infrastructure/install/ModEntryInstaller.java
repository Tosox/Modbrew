package de.tosox.zonerelay.infrastructure.install;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.infrastructure.mo2.Mo2ConfigReader;
import de.tosox.zonerelay.domain.model.EntryType;
import de.tosox.zonerelay.domain.model.Mod;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.port.ArchiveExtractor;
import de.tosox.zonerelay.domain.port.MetaIniWriter;
import de.tosox.zonerelay.domain.port.ModInstaller;
import de.tosox.zonerelay.shared.i18n.Localizer;
import com.google.inject.name.Named;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.config.AppPaths;
import de.tosox.zonerelay.shared.progress.ProgressListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Singleton
public class ModEntryInstaller implements ModInstaller {
	private final Logger fileLogger;
	private final Logger uiLogger;
	private final Localizer localizer;
	private final ArchiveExtractor extractor;
	private final MetaIniWriter metaIniWriter;
	private final Mo2ConfigReader mo2ConfigReader;
	private final AppPaths paths;

	@Inject
	public ModEntryInstaller(@Named("file") Logger fileLogger, @Named("ui") Logger uiLogger,
	                         Localizer localizer, ArchiveExtractor extractor, MetaIniWriter metaIniWriter,
	                         Mo2ConfigReader mo2ConfigReader, AppPaths paths) {
		this.fileLogger = fileLogger;
		this.uiLogger = uiLogger;
		this.localizer = localizer;
		this.extractor = extractor;
		this.metaIniWriter = metaIniWriter;
		this.mo2ConfigReader = mo2ConfigReader;
		this.paths = paths;
	}

	@Override
	public boolean supports(ModEntry entry) {
		return entry.getType() == EntryType.MOD || entry.getType() == EntryType.PATCH;
	}

	@Override
	public void install(ModEntry entry, File archive, ProgressListener progressListener) throws Exception {
		if (!(entry instanceof Mod mod)) {
			throw new IllegalArgumentException("Expected Mod, got " + entry.getClass().getSimpleName());
		}
		progressListener.onProgressUpdate(0, 1);

		Path targetDir;
		if (mod.getType() == EntryType.MOD) {
			targetDir = paths.getModsDir().resolve(mod.getName());
			uiLogger.info(localizer.translate("MSG_ADDON_DELETE_OLD_VERSION"));
			fileLogger.info("Deleting previous version in %s", targetDir);
			FileUtils.deleteDirectory(targetDir.toFile());
		} else {
			targetDir = mo2ConfigReader.getGamePath();
		}

		Path tempDir = paths.getTempDir().resolve(FilenameUtils.removeExtension(archive.getName()));
		Files.createDirectories(tempDir);

		try {
			uiLogger.info(localizer.translate("MSG_EXTRACT_TO", tempDir));
			fileLogger.info("Extracting %s to %s", archive.getPath(), tempDir);
			extractor.extract(archive, tempDir);

			uiLogger.info(localizer.translate("MSG_READ_SETUP"));
			fileLogger.info("Reading setup instructions");

			List<String> setup = mod.getSetup();
			int total = setup.size();

			for (int i = 0; i < total; i++) {
				String instruction = setup.get(i);
				Path source = tempDir.resolve(instruction);
				Path destination = targetDir.resolve(source.getFileName());

				uiLogger.info(localizer.translate("MSG_COPY_TO", instruction, source.getFileName()));
				fileLogger.info("Copying %s → %s", source, destination);
				FileUtils.copyDirectory(source.toFile(), destination.toFile());

				progressListener.onProgressUpdate(i + 1, total);
			}
		} finally {
			fileLogger.info("Cleaning up temp dir: %s", tempDir);
			FileUtils.deleteDirectory(tempDir.toFile());
		}

		if (mod.getType() == EntryType.MOD) {
			uiLogger.info(localizer.translate("MSG_GENERATE_META"));
			metaIniWriter.generate(mod, targetDir);
		}
		progressListener.onProgressUpdate(1, 1);
	}
}

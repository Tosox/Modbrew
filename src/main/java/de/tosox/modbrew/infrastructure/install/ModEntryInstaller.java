package de.tosox.modbrew.infrastructure.install;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.modbrew.infrastructure.mo2.Mo2ConfigReader;
import de.tosox.modbrew.domain.model.EntryType;
import de.tosox.modbrew.domain.model.Mod;
import de.tosox.modbrew.domain.model.ModEntry;
import de.tosox.modbrew.domain.model.SetupPathMissingException;
import de.tosox.modbrew.domain.port.ArchiveExtractor;
import de.tosox.modbrew.domain.port.MetaIniWriter;
import de.tosox.modbrew.domain.port.ModInstaller;
import de.tosox.modbrew.shared.i18n.Localizer;
import com.google.inject.name.Named;
import de.tosox.modbrew.shared.logging.Logger;
import de.tosox.modbrew.shared.config.AppPaths;
import de.tosox.modbrew.shared.progress.ProgressListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
			fileLogger.info("Deleting previous version in %s", paths.relativize(targetDir));
			FileUtils.deleteDirectory(targetDir.toFile());
		} else {
			targetDir = mo2ConfigReader.getGamePath();
		}

		Path tempDir = paths.getTempDir().resolve(FilenameUtils.removeExtension(archive.getName()));
		Files.createDirectories(tempDir);

		try {
			uiLogger.info(localizer.translate("MSG_EXTRACT_TO", paths.relativize(tempDir)));
			extractor.extract(archive, tempDir);

			uiLogger.info(localizer.translate("MSG_READ_RECIPE"));
			fileLogger.info("Reading recipe");

			List<String> recipe = mod.getRecipe();
			int total = recipe.size();

			List<String> missingPaths = new ArrayList<>();
			for (String instruction : recipe) {
				SetupMapping mapping = resolveMapping(instruction, tempDir, targetDir);
				if (!Files.exists(mapping.source())) {
					missingPaths.add(instruction);
				}
			}
			if (!missingPaths.isEmpty()) {
				if (mod.getType() == EntryType.MOD) {
					Files.createDirectories(targetDir);
				}
				throw new SetupPathMissingException(missingPaths);
			}

			for (int i = 0; i < total; i++) {
				String instruction = recipe.get(i);
				SetupMapping mapping = resolveMapping(instruction, tempDir, targetDir);

				uiLogger.info(localizer.translate("MSG_COPY_TO", mapping.source().getFileName(), paths.relativize(mapping.destination())));
				fileLogger.info("Copying %s → %s", paths.relativize(mapping.source()), paths.relativize(mapping.destination()));
				copyEntry(mapping.source(), mapping.destination());

				progressListener.onProgressUpdate(i + 1, total);
			}
		} finally {
			fileLogger.info("Cleaning up temp dir: %s", paths.relativize(tempDir));
			FileUtils.deleteDirectory(tempDir.toFile());
		}

		if (mod.getType() == EntryType.MOD) {
			uiLogger.info(localizer.translate("MSG_GENERATE_META"));
			metaIniWriter.generate(mod, targetDir);
		}
		progressListener.onProgressUpdate(1, 1);
	}

	private SetupMapping resolveMapping(String instruction, Path tempDir, Path targetDir) {
		if (instruction.contains("->")) {
			String[] parts = instruction.split("->", 2);
			Path source = tempDir.resolve(parts[0].trim());
			String dstPart = parts[1].trim();
			Path dstBase = targetDir.resolve(dstPart);

			Path destination;
			if (Files.isDirectory(source) || !FilenameUtils.getExtension(dstPart).isEmpty()) {
				destination = dstBase;
			} else {
				destination = dstBase.resolve(source.getFileName());
			}

			return new SetupMapping(source, destination);
		}

		Path source = tempDir.resolve(instruction);
		return new SetupMapping(source, targetDir.resolve(source.getFileName()));
	}

	private void copyEntry(Path source, Path destination) throws Exception {
		if (Files.isDirectory(source)) {
			FileUtils.copyDirectory(source.toFile(), destination.toFile());
		} else {
			Files.createDirectories(destination.getParent());
			FileUtils.copyFile(source.toFile(), destination.toFile());
		}
	}

	private record SetupMapping(Path source, Path destination) {}
}

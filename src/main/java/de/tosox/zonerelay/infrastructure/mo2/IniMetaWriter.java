package de.tosox.zonerelay.infrastructure.mo2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.model.Mod;
import de.tosox.zonerelay.domain.model.ModEntry;
import de.tosox.zonerelay.domain.model.Separator;
import de.tosox.zonerelay.domain.port.MetaIniWriter;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.config.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class IniMetaWriter implements MetaIniWriter {
	private final Logger logger;
	private final String addonMetaTemplate;
	private final String separatorMetaTemplate;

	@Inject
	public IniMetaWriter(@Named("file") Logger logger, AppPaths paths) throws IOException {
		this.logger = logger;
		this.addonMetaTemplate = Files.readString(paths.getAddonMetaTemplate());
		this.separatorMetaTemplate = Files.readString(paths.getSeparatorMetaTemplate());
	}

	@Override
	public void generate(ModEntry entry, Path targetDir) throws IOException {
		String template;

		if (entry instanceof Mod) {
			template = addonMetaTemplate;
		} else if (entry instanceof Separator) {
			template = separatorMetaTemplate;
		} else {
			throw new IllegalArgumentException("Unsupported entry type for meta.ini: " + entry.getClass());
		}

		String content = template
				.replace("{id}", entry.getId())
				.replace("{name}", entry.getName());

		Files.createDirectories(targetDir);
		Files.writeString(targetDir.resolve("meta.ini"), content);

		logger.info("Generated meta.ini in %s", targetDir);
	}
}

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
	private final Path addonMetaTemplate;
	private final Path separatorMetaTemplate;

	@Inject
	public IniMetaWriter(@Named("file") Logger logger, AppPaths paths) {
		this.logger = logger;
		this.addonMetaTemplate = paths.addonMetaTemplate;
		this.separatorMetaTemplate = paths.separatorMetaTemplate;
	}

	@Override
	public void generate(ModEntry entry, Path targetDir) throws IOException {
		String content;

		if (entry instanceof Mod) {
			content = fillTemplate(entry, addonMetaTemplate);
		} else if (entry instanceof Separator) {
			content = fillTemplate(entry, separatorMetaTemplate);
		} else {
			throw new IllegalArgumentException("Unsupported entry type for meta.ini: " + entry.getClass());
		}

		Files.createDirectories(targetDir);
		Files.writeString(targetDir.resolve("meta.ini"), content);

		logger.info("Generated meta.ini in %s", targetDir);
	}

	private String fillTemplate(ModEntry entry, Path templatePath) throws IOException {
		String template = Files.readString(templatePath);
		return template
				.replace("{id}", entry.getId())
				.replace("{name}", entry.getName());
	}
}

package de.tosox.modbrew.infrastructure.mo2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.modbrew.domain.port.SplashImageCopier;
import de.tosox.modbrew.shared.config.AppPaths;
import de.tosox.modbrew.shared.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Singleton
public class Mo2SplashImageCopier implements SplashImageCopier {
	private final Logger logger;
	private final AppPaths paths;

	@Inject
	public Mo2SplashImageCopier(@Named("file") Logger logger, AppPaths paths) {
		this.logger = logger;
		this.paths = paths;
	}

	@Override
	public void copySplashImage() {
		Path mo2DirSplash = paths.getMo2Dir().resolve("splash.png");

		if (!Files.exists(paths.getMo2Splash())) {
			logger.info("Brewfile splash image not found, skipping");
			return;
		}

		try {
			Files.copy(paths.getMo2Splash(), mo2DirSplash, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException("Failed to copy splash image", e);
		}
	}
}

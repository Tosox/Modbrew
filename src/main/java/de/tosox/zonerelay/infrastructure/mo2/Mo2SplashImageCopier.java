package de.tosox.zonerelay.infrastructure.mo2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.domain.port.SplashImageCopier;
import de.tosox.zonerelay.shared.config.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Singleton
public class Mo2SplashImageCopier implements SplashImageCopier {
	private final AppPaths paths;

	@Inject
	public Mo2SplashImageCopier(AppPaths paths) {
		this.paths = paths;
	}

	@Override
	public void copySplashImage() {
		Path mo2DirSplash = paths.getMo2Dir().resolve("splash.png");

		if (Files.exists(paths.getModlistSplash())) {
			try {
				Files.copy(paths.getModlistSplash(), mo2DirSplash, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new RuntimeException("Failed to copy splash image", e);
			}
		}
	}
}

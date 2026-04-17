package de.tosox.zonerelay.infrastructure.mo2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.domain.port.ProfileSetup;
import de.tosox.zonerelay.shared.config.AppPaths;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class Mo2ProfileSetup implements ProfileSetup {
	private final AppPaths paths;

	@Inject
	public Mo2ProfileSetup(AppPaths paths) {
		this.paths = paths;
	}

	@Override
	public void setupProfile(String profileName) {
		Path newProfilePath = paths.profilesDir.resolve(profileName);

		try {
			FileUtils.copyDirectory(paths.profileFilesDir.toFile(), newProfilePath.toFile());
			FileUtils.copyFile(paths.modlistTxt.toFile(), newProfilePath.resolve("modlist.txt").toFile());
		} catch (IOException e) {
			throw new RuntimeException("Failed to setup MO2 profile", e);
		}
	}
}

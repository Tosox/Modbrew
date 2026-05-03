package de.tosox.modbrew.infrastructure.mo2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.modbrew.domain.port.ProfileSetup;
import de.tosox.modbrew.shared.config.AppPaths;
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
		Path newProfilePath = paths.getProfilesDir().resolve(profileName);

		try {
			FileUtils.copyDirectory(paths.getProfileFilesDir().toFile(), newProfilePath.toFile());
			FileUtils.copyFile(paths.getModlistTxt().toFile(), newProfilePath.resolve("modlist.txt").toFile());
		} catch (IOException e) {
			throw new RuntimeException("Failed to setup MO2 profile", e);
		}
	}
}

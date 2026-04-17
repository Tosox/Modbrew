package de.tosox.zonerelay.infrastructure.mo2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.domain.port.ShortcutCreator;
import de.tosox.zonerelay.shared.config.AppPaths;
import mslinks.ShellLink;

import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class Mo2ShortcutCreator implements ShortcutCreator {
	private final AppPaths paths;

	@Inject
	public Mo2ShortcutCreator(AppPaths paths) {
		this.paths = paths;
	}

	@Override
	public void createShortcut(String shortcutName) {
		try {
			String desktopPath = System.getProperty("user.home") + "/Desktop";
			Path shortcutPath = Path.of(desktopPath, shortcutName + ".lnk");

			ShellLink.createLink(paths.mo2Exe.toAbsolutePath().normalize().toString())
					.setIconLocation(paths.modlistIcon.toAbsolutePath().normalize().toString())
					.saveTo(shortcutPath.toString());
		} catch (IOException e) {
			throw new RuntimeException("Unable to create shortcut", e);
		}
	}
}

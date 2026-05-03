package de.tosox.modbrew.infrastructure.mo2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.modbrew.domain.port.ShortcutCreator;
import de.tosox.modbrew.shared.config.AppPaths;
import mslinks.ShellLink;

import javax.swing.filechooser.FileSystemView;
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
			String desktopPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
			Path shortcutPath = Path.of(desktopPath, shortcutName + ".lnk");

			ShellLink.createLink(paths.getMo2Exe().toAbsolutePath().normalize().toString())
					.setIconLocation(paths.getShortcutIcon().toAbsolutePath().normalize().toString())
					.saveTo(shortcutPath.toString());
		} catch (IOException e) {
			throw new RuntimeException("Unable to create shortcut", e);
		}
	}
}

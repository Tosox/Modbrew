package de.tosox.modbrew.shared.config;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public final class AppPaths {
	private final Path base;
	private final Path mo2Dir;
	private final Path modsDir;
	private final Path profilesDir;
	private final Path downloadsDir;
	private final Path tempDir;
	private final Path mo2Exe;
	private final Path mo2Config;
	private final Path brewfileYaml;
	private final Path modlistTxt;
	private final Path shortcutIcon;
	private final Path mo2Splash;
	private final Path profileFilesDir;
	private final Path sevenZipExe;
	private final Path addonMetaTemplate;
	private final Path separatorMetaTemplate;
	private final Path localesDir;
	private final Path logsDir;
	private final Path progressFile;
	private final Path userConfig;

	private AppPaths(Path base) {
		this.base = base;
		mo2Dir = base.resolve("../").normalize();
		modsDir = mo2Dir.resolve("mods");
		profilesDir = mo2Dir.resolve("profiles");
		downloadsDir = mo2Dir.resolve("downloads");
		tempDir = base.resolve("temp");
		mo2Exe = mo2Dir.resolve("ModOrganizer.exe");
		mo2Config = mo2Dir.resolve("ModOrganizer.ini");
		brewfileYaml = base.resolve("data/modbrew.yaml");
		modlistTxt = base.resolve("data/modlist.txt");
		shortcutIcon = base.resolve("data/assets/icon.ico");
		mo2Splash = base.resolve("data/assets/splash.png");
		profileFilesDir = base.resolve("resources/profile_files");
		sevenZipExe = base.resolve("resources/7zip/7z.exe");
		addonMetaTemplate = base.resolve("resources/addon-meta.ini-template.txt");
		separatorMetaTemplate = base.resolve("resources/separator-meta.ini-template.txt");
		localesDir = base.resolve("locales");
		logsDir = base.resolve("logs");
		progressFile = base.resolve("install_progress.dat");
		userConfig = base.resolve("user_config.yaml");
	}

	public Path relativize(Path path) {
		try {
			return base.relativize(path.toAbsolutePath().normalize());
		} catch (IllegalArgumentException e) {
			return path;
		}
	}

	public static AppPaths fromBase(Path baseDir) {
		return new AppPaths(baseDir.toAbsolutePath().normalize());
	}
}

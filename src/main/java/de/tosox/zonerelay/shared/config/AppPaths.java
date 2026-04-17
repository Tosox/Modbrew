package de.tosox.zonerelay.shared.config;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public final class AppPaths {
	private final Path mo2Dir;
	private final Path modsDir;
	private final Path profilesDir;
	private final Path downloadsDir;
	private final Path tempDir;
	private final Path mo2Exe;
	private final Path mo2Config;
	private final Path modlistYaml;
	private final Path modlistTxt;
	private final Path modlistIcon;
	private final Path modlistSplash;
	private final Path profileFilesDir;
	private final Path sevenZipExe;
	private final Path addonMetaTemplate;
	private final Path separatorMetaTemplate;
	private final Path localesDir;
	private final Path logsDir;
	private final Path progressFile;
	private final Path userConfig;

	private AppPaths(Path base) {
		mo2Dir = base.resolve("../").normalize();
		modsDir = mo2Dir.resolve("mods");
		profilesDir = mo2Dir.resolve("profiles");
		downloadsDir = mo2Dir.resolve("downloads");
		tempDir = base.resolve("temp");
		mo2Exe = mo2Dir.resolve("ModOrganizer.exe");
		mo2Config = mo2Dir.resolve("ModOrganizer.ini");
		modlistYaml = base.resolve("data/modlist.yaml");
		modlistTxt = base.resolve("data/modlist.txt");
		modlistIcon = base.resolve("data/assets/icon.ico");
		modlistSplash = base.resolve("data/assets/splash.png");
		profileFilesDir = base.resolve("resources/profile_files");
		sevenZipExe = base.resolve("resources/7zip/7z.exe");
		addonMetaTemplate = base.resolve("resources/addon-meta.ini-template.txt");
		separatorMetaTemplate = base.resolve("resources/separator-meta.ini-template.txt");
		localesDir = base.resolve("locales");
		logsDir = base.resolve("logs");
		progressFile = base.resolve("install_progress.dat");
		userConfig = base.resolve("user_config.yaml");
	}

	public static AppPaths fromBase(Path baseDir) {
		return new AppPaths(baseDir.toAbsolutePath().normalize());
	}
}

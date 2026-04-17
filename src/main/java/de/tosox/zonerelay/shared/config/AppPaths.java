package de.tosox.zonerelay.shared.config;

import java.nio.file.Path;

/**
 * All file-system paths used by the application, constructed from the working directory.
 * Injected as a singleton so no class needs to hardcode or compute paths independently.
 */
public final class AppPaths {
	public final Path mo2Dir;
	public final Path modsDir;
	public final Path profilesDir;
	public final Path downloadsDir;
	public final Path tempDir;
	public final Path mo2Exe;
	public final Path mo2Config;
	public final Path modlistYaml;
	public final Path modlistTxt;
	public final Path modlistIcon;
	public final Path modlistSplash;
	public final Path profileFilesDir;
	public final Path sevenZipExe;
	public final Path addonMetaTemplate;
	public final Path separatorMetaTemplate;
	public final Path localesDir;
	public final Path logsDir;
	public final Path progressFile;

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
	}

	public static AppPaths fromBase(Path baseDir) {
		return new AppPaths(baseDir.toAbsolutePath().normalize());
	}
}

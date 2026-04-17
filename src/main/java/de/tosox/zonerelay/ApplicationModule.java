package de.tosox.zonerelay;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.infrastructure.mo2.Mo2ConfigReader;
import de.tosox.zonerelay.shared.config.UserSettings;
import de.tosox.zonerelay.domain.port.*;
import de.tosox.zonerelay.infrastructure.download.OkHttpArchiveDownloader;
import de.tosox.zonerelay.infrastructure.download.source.DirectUrlSource;
import de.tosox.zonerelay.infrastructure.download.source.ModDbUrlSource;
import de.tosox.zonerelay.infrastructure.download.source.UrlSource;
import de.tosox.zonerelay.infrastructure.extraction.SevenZipExtractor;
import de.tosox.zonerelay.infrastructure.install.ModEntryInstaller;
import de.tosox.zonerelay.infrastructure.install.SeparatorInstaller;
import de.tosox.zonerelay.infrastructure.mo2.*;
import de.tosox.zonerelay.infrastructure.persistence.FileInstallProgressStore;
import de.tosox.zonerelay.infrastructure.persistence.YamlModlistRepository;
import de.tosox.zonerelay.shared.i18n.Localizer;
import de.tosox.zonerelay.shared.logging.LogManager;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.config.AppPaths;
import de.tosox.zonerelay.ui.util.ImageLoader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ArchiveDownloader.class).to(OkHttpArchiveDownloader.class);
		bind(ArchiveExtractor.class).to(SevenZipExtractor.class);
		bind(InstallProgressStore.class).to(FileInstallProgressStore.class);
		bind(MetaIniWriter.class).to(IniMetaWriter.class);
		bind(ModlistRepository.class).to(YamlModlistRepository.class);
		bind(ProfileSetup.class).to(Mo2ProfileSetup.class);
		bind(ShortcutCreator.class).to(Mo2ShortcutCreator.class);
		bind(SplashImageCopier.class).to(Mo2SplashImageCopier.class);
	}

	@Provides
	@Singleton
	List<ModInstaller> provideInstallers(ModEntryInstaller modInstaller, SeparatorInstaller separatorInstaller) {
		return List.of(modInstaller, separatorInstaller);
	}

	@Provides
	@Singleton
	List<UrlSource> provideUrlSources(ModDbUrlSource modDb) {
		return List.of(modDb, new DirectUrlSource()); // specific first, fallback last
	}

	@Provides
	@Singleton
	AppPaths provideAppPaths() {
		return AppPaths.fromBase(Path.of("").toAbsolutePath());
	}

	@Provides
	@Singleton
	JTextPane provideOutputPane() {
		return new JTextPane();
	}

	@Provides
	@Singleton
	LogManager provideLogManager(JTextPane outputPane, AppPaths paths) {
		return new LogManager(outputPane, paths.getLogsDir());
	}

	@Provides
	@Named("file")
	@Singleton
	Logger provideFileLogger(LogManager logManager) {
		return logManager.getFileLogger();
	}

	@Provides
	@Named("ui")
	@Singleton
	Logger provideUiLogger(LogManager logManager) {
		return logManager.getUiLogger();
	}

	@Provides
	@Singleton
	UserSettings provideUserSettings() {
		return UserSettings.load(new File("user_config.yaml"));
	}

	@Provides
	@Singleton
	Localizer provideLocalizer(UserSettings settings, @Named("file") Logger logger, AppPaths paths) throws IOException {
		return new Localizer(settings.getLanguage(), logger, paths.getLocalesDir());
	}

	@Provides
	@Singleton
	Mo2ConfigReader provideMo2ConfigReader(AppPaths paths) {
		return new Mo2ConfigReader(paths.getMo2Config());
	}

	@Provides
	@Singleton
	ImageLoader provideImageLoader() throws IOException {
		return new ImageLoader();
	}
}

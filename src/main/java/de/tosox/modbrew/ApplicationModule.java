package de.tosox.modbrew;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.modbrew.infrastructure.mo2.Mo2ConfigReader;
import de.tosox.modbrew.shared.config.UserSettings;
import de.tosox.modbrew.infrastructure.config.UserSettingsManager;
import de.tosox.modbrew.domain.port.*;
import de.tosox.modbrew.infrastructure.download.HttpArchiveDownloader;
import de.tosox.modbrew.infrastructure.download.source.DirectUrlSource;
import de.tosox.modbrew.infrastructure.download.source.ModDbUrlSource;
import de.tosox.modbrew.infrastructure.download.source.UrlSource;
import de.tosox.modbrew.infrastructure.extraction.SevenZipExtractor;
import de.tosox.modbrew.infrastructure.install.ModEntryInstaller;
import de.tosox.modbrew.infrastructure.install.SeparatorInstaller;
import de.tosox.modbrew.infrastructure.mo2.*;
import de.tosox.modbrew.infrastructure.persistence.FileInstallProgressStore;
import de.tosox.modbrew.infrastructure.persistence.YamlBrewfileRepository;
import de.tosox.modbrew.shared.i18n.Localizer;
import de.tosox.modbrew.shared.logging.LogManager;
import de.tosox.modbrew.shared.logging.Logger;
import de.tosox.modbrew.shared.config.AppPaths;
import de.tosox.modbrew.ui.util.ImageLoader;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ArchiveDownloader.class).to(HttpArchiveDownloader.class);
		bind(ArchiveExtractor.class).to(SevenZipExtractor.class);
		bind(InstallProgressStore.class).to(FileInstallProgressStore.class);
		bind(MetaIniWriter.class).to(IniMetaWriter.class);
		bind(BrewfileRepository.class).to(YamlBrewfileRepository.class);
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
		// NOTE: Specific first, fallback last
		return List.of(modDb, new DirectUrlSource());
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
	UserSettingsManager provideUserSettingsManager(AppPaths paths) {
		return new UserSettingsManager(paths.getUserConfig().toFile());
	}

	@Provides
	@Singleton
	UserSettings provideUserSettings(UserSettingsManager manager) {
		return manager.getCurrent();
	}

	@Provides
	@Singleton
	LogManager provideLogManager(JTextPane outputPane, AppPaths paths, UserSettings settings) {
		return new LogManager(outputPane, paths.getLogsDir(), settings.getLogLevel(), settings.getLogRetentionCount());
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

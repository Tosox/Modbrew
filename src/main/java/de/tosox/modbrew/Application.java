package de.tosox.modbrew;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import de.tosox.modbrew.shared.config.AppPaths;
import de.tosox.modbrew.ui.CrashHandler;
import de.tosox.modbrew.ui.MainFrame;
import de.tosox.modbrew.shared.i18n.Localizer;
import de.tosox.modbrew.shared.logging.Logger;

public class Application {
	private final MainFrame mainFrame;

	public Application() {
		Injector injector = Guice.createInjector(new ApplicationModule());
		CrashHandler crashHandler = injector.getInstance(CrashHandler.class);
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
				crashHandler.fatal("Unexpected error in thread: " + thread.getName(),
						throwable instanceof Exception ex ? ex : new RuntimeException(throwable)));

		this.mainFrame = injector.getInstance(MainFrame.class);

		Logger fileLogger = injector.getInstance(Key.get(Logger.class, Names.named("file")));
		Logger uiLogger = injector.getInstance(Key.get(Logger.class, Names.named("ui")));
		Localizer localizer = injector.getInstance(Localizer.class);
		AppPaths paths = injector.getInstance(AppPaths.class);

		fileLogger.info("Working directory: %s", paths.getBase());
		uiLogger.info(localizer.translate("MSG_WELCOME_MESSAGE", BuildInfo.APP_NAME));
		uiLogger.info("-------------------------------------------------------------------\n");
	}

	public void start() {
		mainFrame.showWindow();
	}
}

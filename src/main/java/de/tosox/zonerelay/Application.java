package de.tosox.zonerelay;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.tosox.zonerelay.ui.MainFrame;
import de.tosox.zonerelay.shared.i18n.Localizer;
import de.tosox.zonerelay.shared.logging.LogManager;

public class Application {
	private final MainFrame mainFrame;

	public Application() {
		Injector injector = Guice.createInjector(new ApplicationModule());
		this.mainFrame = injector.getInstance(MainFrame.class);

		LogManager logManager = injector.getInstance(LogManager.class);
		Localizer localizer = injector.getInstance(Localizer.class);

		logManager.getUiLogger().info(localizer.translate("MSG_WELCOME_MESSAGE", BuildInfo.APP_NAME));
		logManager.getUiLogger().info("-------------------------------------------------------------------\n");
	}

	public void start() {
		mainFrame.showWindow();
	}
}

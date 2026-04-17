package de.tosox.zonerelay.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.shared.logging.Logger;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

@Singleton
public class CrashHandler {
	private final Logger logger;

	@Inject
	public CrashHandler(@Named("file") Logger logger) {
		this.logger = logger;
	}

	public void fatal(String message, Exception e) {
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));

		logger.error("%s%n%s", message, stringWriter);
		System.err.printf("[FATAL] %s%n%s%n", message, stringWriter);

		try {
			if (SwingUtilities.isEventDispatchThread()) {
				showDialog(message);
			} else {
				SwingUtilities.invokeAndWait(() -> showDialog(message));
			}
		} catch (Exception ignored) {}

		System.exit(1);
	}

	private void showDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}

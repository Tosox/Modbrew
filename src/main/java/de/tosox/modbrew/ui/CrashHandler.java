package de.tosox.modbrew.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.modbrew.shared.logging.Logger;

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

	public void fatal(String message, Exception exception) {
		StringWriter stringWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stringWriter));

		logger.error("%s%n%s", message, stringWriter);
		System.err.printf("[FATAL] %s%n%s%n", message, stringWriter);

		try {
			if (SwingUtilities.isEventDispatchThread()) {
				showDialog(message);
			} else {
				SwingUtilities.invokeAndWait(() -> showDialog(message));
			}
		} catch (Exception e) {
			System.err.printf("[FATAL] Failed to show error dialog: %s%n", e.getMessage());
		}

		System.exit(1);
	}

	private void showDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}

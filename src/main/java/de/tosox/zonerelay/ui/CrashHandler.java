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

		logger.error(message, stringWriter);

		JOptionPane.showMessageDialog(
				null,
				message,
				"Error",
				JOptionPane.ERROR_MESSAGE
		);

		System.exit(1);
	}
}

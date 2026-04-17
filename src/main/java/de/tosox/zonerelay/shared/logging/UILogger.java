package de.tosox.zonerelay.shared.logging;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class UILogger implements Logger {
	private final JTextPane outputPane;

	public UILogger(JTextPane outputPane) {
		this.outputPane = outputPane;
	}

	private void append(String text, Color color) {
		SwingUtilities.invokeLater(() -> {
			StyledDocument doc = outputPane.getStyledDocument();
			Style style = outputPane.addStyle("style", null);
			StyleConstants.setForeground(style, color);
			StyleConstants.setFontFamily(style, "monospaced");

			try {
				doc.insertString(doc.getLength(), text + "\n", style);
				outputPane.setCaretPosition(doc.getLength());
			} catch (BadLocationException ignored) {}
		});
	}

	@Override
	public void debug(String message) {}

	@Override
	public void debug(String message, Object... args) {}

	@Override
	public void info(String message) {
		append(message, new Color(0xC2C2C2));
	}

	@Override
	public void info(String message, Object... args) {
		info(String.format(message, args));
	}

	@Override
	public void warn(String message) {
		append(message, new Color(0xFFCC00));
	}

	@Override
	public void warn(String message, Object... args) {
		warn(String.format(message, args));
	}

	@Override
	public void error(String message) {
		append(message, new Color(0xFF4444));
	}

	@Override
	public void error(String message, Object... args) {
		error(String.format(message, args));
	}
}

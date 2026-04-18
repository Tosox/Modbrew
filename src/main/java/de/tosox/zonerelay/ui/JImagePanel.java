package de.tosox.zonerelay.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JImagePanel extends JPanel {
	private final BufferedImage image;
	private Image scaledImage;
	private int lastWidth = -1;
	private int lastHeight = -1;

	public JImagePanel(BufferedImage image) {
		this.image = image;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(getScaledToBoundsInstance(), 0, 0, this);
		}
	}

	private Image getScaledToBoundsInstance() {
		int w = getWidth();
		int h = getHeight();
		if (scaledImage == null || w != lastWidth || h != lastHeight) {
			scaledImage = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
			lastWidth = w;
			lastHeight = h;
		}
		return scaledImage;
	}
}

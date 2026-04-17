package de.tosox.zonerelay.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JImagePanel extends JPanel {
	private final BufferedImage image;

	public JImagePanel(Image image) {
		this.image = (BufferedImage) image;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (image != null) {
			g.drawImage(getScaledToBoundsInstance(), 0, 0, this);
		}
	}

	private Image getScaledToBoundsInstance() {
		return image.getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_SMOOTH);
	}
}

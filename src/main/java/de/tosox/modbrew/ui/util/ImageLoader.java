package de.tosox.modbrew.ui.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageLoader {
	private final BufferedImage fallbackImage;

	public ImageLoader() throws IOException {
		this.fallbackImage = loadFallbackImage();
	}

	public BufferedImage load(URL resource) {
		if (resource == null) {
			return fallbackImage;
		}

		try {
			BufferedImage image = ImageIO.read(resource);
			return image != null ? image : fallbackImage;
		} catch (IOException e) {
			return fallbackImage;
		}
	}

	public BufferedImage resize(BufferedImage image, int width, int height) {
		return toBufferedImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}

	public BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage buffered) {
			return buffered;
		}

		BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.drawImage(img, 0, 0, null);
		graphics.dispose();
		return image;
	}

	private BufferedImage loadFallbackImage() throws IOException {
		URL resource = getClass().getResource("missing_texture.png");
		if (resource == null) {
			throw new IOException("Missing fallback image resource");
		}

		try {
			BufferedImage image = ImageIO.read(resource);
			if (image == null) {
				throw new IOException("Fallback image was null");
			}
			return image;
		} catch (IOException e) {
			throw new IOException("Could not load fallback image", e);
		}
	}
}

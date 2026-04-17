package de.tosox.zonerelay.infrastructure.download;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.shared.logging.Logger;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class FilenameResolver {
	private static final Pattern FILENAME_FROM_CONTENT_DISPOSITION =
			Pattern.compile("filename\\*?=['\"]?(?:UTF-\\d['\"]*)?([^;\"']*)['\"]?;?");

	private final Logger logger;
	private final HttpClient httpClient;

	@Inject
	public FilenameResolver(@Named("file") Logger logger, HttpClient httpClient) {
		this.logger = logger;
		this.httpClient = httpClient;
	}

	public String resolve(String directUrl) throws IOException {
		Request request = new Request.Builder()
				.url(directUrl)
				.head()
				.build();

		try (Response response = httpClient.execute(request)) {
			String fromHeader = extractFromContentDisposition(response);
			if (fromHeader != null) {
				return fromHeader;
			}

			String finalUrl = response.request().url().toString();
			String fromUrl = extractFilenameFromUrl(finalUrl);
			if (fromUrl != null) {
				return fromUrl;
			}
		}

		throw new IOException("Could not resolve filename for URL: " + directUrl);
	}

	private String extractFromContentDisposition(Response response) {
		String header = response.header("Content-Disposition");
		if (header != null) {
			Matcher matcher = FILENAME_FROM_CONTENT_DISPOSITION.matcher(header);
			if (matcher.find()) {
				String fileName = matcher.group(1);
				logger.info("Filename from Content-Disposition: %s", fileName);
				return fileName;
			}
		}
		logger.info("No valid Content-Disposition header found");
		return null;
	}

	private String extractFilenameFromUrl(String url) {
		String path = URI.create(url).getPath();
		if (path == null || path.isEmpty()) {
			logger.warn("URL path is empty: %s", url);
			return null;
		}

		String fileName = path.substring(path.lastIndexOf('/') + 1);
		if (!fileName.isEmpty()) {
			logger.info("Filename from URL: %s", fileName);
			return fileName;
		}

		return null;
	}
}

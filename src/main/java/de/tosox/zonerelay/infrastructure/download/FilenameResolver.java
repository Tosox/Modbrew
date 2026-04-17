package de.tosox.zonerelay.infrastructure.download;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.shared.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class FilenameResolver {
	private static final Pattern FILENAME_FROM_CONTENT_DISPOSITION =
			Pattern.compile("filename\\*?=['\"]?(?:UTF-\\d['\"]*)?([^;\"']*)['\"]?;?");
	private static final OkHttpClient CLIENT = new OkHttpClient();

	private final Logger logger;

	@Inject
	public FilenameResolver(@Named("file") Logger logger) {
		this.logger = logger;
	}

	public String resolve(String directUrl) {
		String filename = fetchFilename(directUrl);
		if (filename == null) {
			throw new IllegalStateException("Could not resolve filename for URL " + directUrl);
		}
		return filename;
	}

	private String fetchFilename(String url) {
		Request request = new Request.Builder()
				.url(url)
				.head()
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			String fromHeader = extractFromContentDisposition(response);
			if (fromHeader != null) {
				return fromHeader;
			}

			String finalUrl = response.request().url().toString();
			return extractFilenameFromUrl(finalUrl);
		} catch (IOException e) {
			logger.warn("HEAD request failed for URL %s: %s", url, e.getMessage());
			return null;
		}
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
		try {
			URL parsed = new URL(url);
			String path = parsed.getPath();
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
		} catch (MalformedURLException e) {
			logger.error("Malformed URL while extracting filename: %s", url);
			return null;
		}
	}
}

package de.tosox.zonerelay.infrastructure.download.source;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.infrastructure.download.HttpClient;
import de.tosox.zonerelay.shared.logging.Logger;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

@Singleton
public class ModDbUrlSource implements UrlSource {
	private static final String MODDB_HOST = "moddb.com";
	private static final String MODDB_BASE = "https://www.moddb.com";

	private final Logger logger;
	private final HttpClient httpClient;

	@Inject
	public ModDbUrlSource(@Named("file") Logger logger, HttpClient httpClient) {
		this.logger = logger;
		this.httpClient = httpClient;
	}

	@Override
	public boolean supports(String url) {
		return url.contains(MODDB_HOST);
	}

	@Override
	public String resolve(String url) throws Exception {
		try {
			Document addonPage = fetchPage(url);
			Element downloadElem = addonPage.getElementById("downloadmirrorstoggle");
			if (downloadElem == null) {
				logger.error("Download element not found on ModDB page.");
				return null;
			}

			String relDownloadUrl = downloadElem.attr("href");
			String downloadPageUrl = MODDB_BASE + relDownloadUrl;
			Document downloadPage = fetchPage(downloadPageUrl);

			// TODO: Resolve best mirror
			Element downloadLinkElement = downloadPage.selectFirst("p a:first-child");
			if (downloadLinkElement == null) {
				logger.error("Download link not found on ModDB download page.");
				return null;
			}

			String relDownloadLink = downloadLinkElement.attr("href");
			return MODDB_BASE + relDownloadLink;
		} catch (Exception e) {
			logger.error("Error resolving ModDB download link: %s", e.getMessage());
			throw e;
		}
	}

	private Document fetchPage(String url) throws IOException {
		Request request = new Request.Builder()
				.url(url)
				.build();

		try (Response response = httpClient.execute(request)) {
			if (!response.isSuccessful()) {
				throw new IOException("Failed to load page: " + url + " (Status code: " + response.code() + ")");
			}

			return Jsoup.parse(response.body().string());
		}
	}
}

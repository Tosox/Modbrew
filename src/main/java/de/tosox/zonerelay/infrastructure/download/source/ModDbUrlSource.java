package de.tosox.zonerelay.infrastructure.download.source;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.infrastructure.download.HttpClient;
import de.tosox.zonerelay.infrastructure.download.ResolveResult;
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
	public ResolveResult resolve(String url) throws Exception {
		Document addonPage = fetchPage(url);
		Element downloadElem = addonPage.getElementById("downloadmirrorstoggle");
		if (downloadElem == null) {
			throw new IOException("Download element not found on ModDB page: " + url);
		}

		String relDownloadUrl = downloadElem.attr("href");
		String downloadPageUrl = MODDB_BASE + relDownloadUrl;
		Document downloadPage = fetchPage(downloadPageUrl);

		// TODO: Resolve best mirror
		Element downloadLinkElement = downloadPage.selectFirst("p a:first-child");
		if (downloadLinkElement == null) {
			throw new IOException("Download link not found on ModDB download page: " + downloadPageUrl);
		}

		String relDownloadLink = downloadLinkElement.attr("href");
		String resolvedUrl = MODDB_BASE + relDownloadLink;

		String hash = scrapeHash(addonPage);
		if (hash != null) {
			logger.info("Scraped ModDB MD5 hash: %s", hash);
		}

		return ResolveResult.of(resolvedUrl, hash);
	}

	private String scrapeHash(Document downloadPage) {
		Element h5 = downloadPage.selectFirst("h5:contains(MD5 Hash)");
		if (h5 == null) {
			return null;
		}
		Element span = h5.nextElementSibling();
		if (span == null) {
			return null;
		}
		String hash = span.text().trim().toLowerCase();
		return hash.isEmpty() ? null : hash;
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

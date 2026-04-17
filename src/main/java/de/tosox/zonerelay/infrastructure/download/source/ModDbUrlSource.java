package de.tosox.zonerelay.infrastructure.download.source;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.shared.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

@Singleton
public class ModDbUrlSource implements UrlSource {
	private static final OkHttpClient CLIENT = new OkHttpClient();
	private static final String MODDB_HOST = "moddb.com";
	private static final String MODDB_BASE = "https://moddb.com/";
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux i686; rv:57.0) Gecko/20100101 Firefox/57.0";

	private final Logger logger;

	@Inject
	public ModDbUrlSource(@Named("file") Logger logger) {
		this.logger = logger;
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

			Element downloadLinkElement = downloadPage.selectFirst("a[href]");
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
				.header("User-Agent", USER_AGENT)
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Failed to load page: " + url + " (Status code: " + response.code() + ")");
			}

			ResponseBody body = response.body();
			if (body == null) {
				throw new IOException("Empty response body for URL: " + url);
			}

			return Jsoup.parse(body.string());
		}
	}
}

package de.tosox.zonerelay.infrastructure.download;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.port.ArchiveDownloader;
import de.tosox.zonerelay.infrastructure.download.source.UrlSource;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.progress.ProgressInputStream;
import de.tosox.zonerelay.shared.progress.ProgressListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Singleton
public class OkHttpArchiveDownloader implements ArchiveDownloader {
	private static final OkHttpClient CLIENT = new OkHttpClient();
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux i686; rv:57.0) Gecko/20100101 Firefox/57.0";

	private final Logger logger;
	private final List<UrlSource> urlSources;
	private final FilenameResolver filenameResolver;

	@Inject
	public OkHttpArchiveDownloader(@Named("file") Logger logger, List<UrlSource> urlSources,
	                               FilenameResolver filenameResolver) {
		this.logger = logger;
		this.urlSources = urlSources;
		this.filenameResolver = filenameResolver;
	}

	@Override
	public File download(String url, File destination, ProgressListener listener) throws Exception {
		String resolvedUrl = resolveUrl(url);

		String filename = filenameResolver.resolve(resolvedUrl);
		File archive = new File(destination, filename);

		if (archive.isFile()) {
			logger.info("Archive already downloaded, skipping: %s", archive.getPath());
			return archive;
		}

		logger.info("Downloading %s", url);

		Request request = new Request.Builder()
				.url(resolvedUrl)
				.header("User-Agent", USER_AGENT)
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Download failed: " + response.code() + " " + response.message());
			}

			ResponseBody body = response.body();
			try (ProgressInputStream inputStream = new ProgressInputStream(body.byteStream(), body.contentLength(), listener);
			     FileOutputStream outputStream = new FileOutputStream(archive)) {
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
			}
		}

		logger.info("Downloaded to %s", archive.getPath());
		return archive;
	}

	private String resolveUrl(String url) throws Exception {
		for (UrlSource source : urlSources) {
			if (source.supports(url)) {
				return source.resolve(url);
			}
		}
		return url; // should not happen if DirectUrlSource is in the list
	}
}

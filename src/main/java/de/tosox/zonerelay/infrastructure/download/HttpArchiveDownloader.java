package de.tosox.zonerelay.infrastructure.download;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.port.ArchiveDownloader;
import de.tosox.zonerelay.infrastructure.download.source.UrlSource;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.progress.ProgressInputStream;
import de.tosox.zonerelay.shared.progress.ProgressListener;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Singleton
public class HttpArchiveDownloader implements ArchiveDownloader {
	private final Logger logger;
	private final HttpClient httpClient;
	private final List<UrlSource> urlSources;
	private final FilenameResolver filenameResolver;

	@Inject
	public HttpArchiveDownloader(@Named("file") Logger logger, HttpClient httpClient,
	                             List<UrlSource> urlSources, FilenameResolver filenameResolver) {
		this.logger = logger;
		this.httpClient = httpClient;
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
				.build();

		try (Response response = httpClient.execute(request)) {
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

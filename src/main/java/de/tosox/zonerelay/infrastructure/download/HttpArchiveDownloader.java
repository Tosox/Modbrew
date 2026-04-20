package de.tosox.zonerelay.infrastructure.download;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.zonerelay.domain.port.ArchiveDownloader;
import de.tosox.zonerelay.domain.port.DownloadResult;
import de.tosox.zonerelay.infrastructure.download.source.UrlSource;
import de.tosox.zonerelay.shared.logging.Logger;
import de.tosox.zonerelay.shared.progress.ProgressInputStream;
import de.tosox.zonerelay.shared.progress.ProgressListener;
import de.tosox.zonerelay.shared.util.HashUtils;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Singleton
public class HttpArchiveDownloader implements ArchiveDownloader {
	private final Logger logger;
	private final HttpClient httpClient;
	private final List<UrlSource> urlSources;
	private final FilenameResolver filenameResolver;
	private final DownloadsManifestStore manifestStore;

	@Inject
	public HttpArchiveDownloader(@Named("file") Logger logger, HttpClient httpClient,
	                             List<UrlSource> urlSources, FilenameResolver filenameResolver,
	                             DownloadsManifestStore manifestStore) {
		this.logger = logger;
		this.httpClient = httpClient;
		this.urlSources = urlSources;
		this.filenameResolver = filenameResolver;
		this.manifestStore = manifestStore;
	}

	@Override
	public DownloadResult download(String url, String modId, String declaredHash, File destination, ProgressListener listener) throws Exception {
		ResolveResult resolved = resolveUrl(url);

		Optional<DownloadResult> cached = tryServeFromCache(url, modId, declaredHash, destination, resolved);
		if (cached.isPresent()) {
			return cached.get();
		}

		String filename = filenameResolver.resolve(resolved.url());
		File archive = new File(destination, filename);

		logger.info("Downloading %s", url);

		Request request = new Request.Builder()
				.url(resolved.url())
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
		} catch (Exception e) {
			FileUtils.deleteQuietly(archive);
			throw e;
		}

		String computedHash = HashUtils.md5(archive);
		manifestStore.recordDownload(modId, url, filename);

		logger.info("Downloaded to %s", archive.getPath());
		return new DownloadResult(archive, computedHash);
	}

	private Optional<DownloadResult> tryServeFromCache(String url, String modId, String declaredHash,
	                                                    File destination, ResolveResult resolved) throws IOException {
		ManifestEntry cached = manifestStore.getManifest().getEntry(modId);
		if (cached == null || !cached.url().equals(url)) {
			return Optional.empty();
		}

		File cachedArchive = new File(destination, cached.filename());
		if (!cachedArchive.isFile()) {
			return Optional.empty();
		}

		if (resolved.hasHash()) {
			String installedHash = cached.installedHash();
			if (installedHash != null && installedHash.equalsIgnoreCase(resolved.hash())) {
				logger.info("Archive unchanged on server (scraped hash match), skipping: %s", cachedArchive.getPath());
				return Optional.of(new DownloadResult(cachedArchive, installedHash));
			}
		} else if (declaredHash != null) {
			String cachedFileHash = HashUtils.md5(cachedArchive);
			if (declaredHash.equalsIgnoreCase(cachedFileHash)) {
				logger.info("Archive already downloaded (declared hash match), skipping: %s", cachedArchive.getPath());
				return Optional.of(new DownloadResult(cachedArchive, cachedFileHash));
			}
			logger.info("Cached archive does not match declared hash, re-downloading: %s", cachedArchive.getPath());
		}

		return Optional.empty();
	}

	private ResolveResult resolveUrl(String url) throws Exception {
		for (UrlSource source : urlSources) {
			if (source.supports(url)) {
				return source.resolve(url);
			}
		}
		throw new IllegalStateException("No URL source found for: " + url);
	}
}

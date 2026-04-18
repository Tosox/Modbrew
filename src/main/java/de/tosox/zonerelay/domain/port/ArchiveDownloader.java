package de.tosox.zonerelay.domain.port;

import de.tosox.zonerelay.shared.progress.ProgressListener;

import java.io.File;

public interface ArchiveDownloader {
	File download(String url, File destination, ProgressListener listener) throws Exception;
}

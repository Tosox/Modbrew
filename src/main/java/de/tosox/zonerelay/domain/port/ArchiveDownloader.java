package de.tosox.zonerelay.domain.port;

import de.tosox.zonerelay.shared.progress.ProgressListener;

import java.io.File;

public interface ArchiveDownloader {
	DownloadResult download(String url, String modId, String declaredHash, File destination, boolean fullInstall, ProgressListener listener) throws Exception;
}

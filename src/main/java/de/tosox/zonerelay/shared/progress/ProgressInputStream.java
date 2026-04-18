package de.tosox.zonerelay.shared.progress;

import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends FilterInputStream {
	private final long totalBytes;
	private final ProgressListener listener;
	private long bytesRead;

	public ProgressInputStream(InputStream in, long totalBytes, ProgressListener listener) {
		super(in);
		this.totalBytes = totalBytes;
		this.listener = listener;
		this.bytesRead = 0;
	}

	@Override
	public int read(@NotNull byte[] b, int off, int len) throws IOException {
		int n = super.read(b, off, len);
		if (n > 0) {
			bytesRead += n;
			if (listener != null) {
				listener.onProgressUpdate(bytesRead, totalBytes);
			}
		}
		return n;
	}

	@Override
	public int read() throws IOException {
		int n = super.read();
		if (n != -1) {
			bytesRead++;
			if (listener != null) {
				listener.onProgressUpdate(bytesRead, totalBytes);
			}
		}
		return n;
	}
}

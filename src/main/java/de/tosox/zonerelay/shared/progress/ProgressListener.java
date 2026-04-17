package de.tosox.zonerelay.shared.progress;

@FunctionalInterface
public interface ProgressListener {
	void onProgressUpdate(long current, long total);
}

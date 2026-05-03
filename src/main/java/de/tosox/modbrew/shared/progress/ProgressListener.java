package de.tosox.modbrew.shared.progress;

@FunctionalInterface
public interface ProgressListener {
	void onProgressUpdate(long current, long total);
}

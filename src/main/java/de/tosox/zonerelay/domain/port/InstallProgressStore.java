package de.tosox.zonerelay.domain.port;

public interface InstallProgressStore {
	void save(String entryId);
	boolean hasSavedState();
	String load();
	void clear();
}

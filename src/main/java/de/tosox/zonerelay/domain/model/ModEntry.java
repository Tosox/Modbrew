package de.tosox.zonerelay.domain.model;

import lombok.Getter;

@Getter
public abstract class ModEntry {
	private final String id;
	private final EntryType type;
	private final String name;

	public ModEntry(String id, EntryType type, String name) {
		this.id = id;
		this.type = type;
		this.name = name;
	}
}

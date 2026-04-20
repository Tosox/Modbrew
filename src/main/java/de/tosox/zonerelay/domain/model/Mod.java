package de.tosox.zonerelay.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Mod extends ModEntry {
	private final String url;
	private final List<String> setup;
	private final String hash;

	@JsonCreator
	public Mod(@JsonProperty("id") String id,
	           @JsonProperty("name") String name,
	           @JsonProperty("url") String url,
			   @JsonProperty("hash") String hash,
	           @JsonProperty("setup") List<String> setup) {
		this(id, EntryType.MOD, name, url, hash, setup);
	}

	protected Mod(String id, EntryType type, String name, String url, String hash, List<String> setup) {
		super(id, type, name);
		this.url = url;
		this.hash = hash;
		this.setup = setup != null ? setup : List.of();
	}

	public boolean hasHash() {
		return hash != null && !hash.isBlank();
	}
}

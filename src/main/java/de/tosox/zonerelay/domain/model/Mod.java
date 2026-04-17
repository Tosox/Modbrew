package de.tosox.zonerelay.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Mod extends ModEntry {
	private final String url;
	private final List<String> setup;

	@JsonCreator
	public Mod(@JsonProperty("id") String id,
	           @JsonProperty("name") String name,
	           @JsonProperty("url") String url,
	           @JsonProperty("setup") List<String> setup) {
		super(id, EntryType.MOD, name);
		this.url = url;
		this.setup = setup;
	}

	protected Mod(String id, EntryType type, String name, String url, List<String> setup) {
		super(id, type, name);
		this.url = url;
		this.setup = setup != null ? setup : List.of();
	}
}

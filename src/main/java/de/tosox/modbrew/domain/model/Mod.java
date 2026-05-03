package de.tosox.modbrew.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Mod extends ModEntry {
	private final String url;
	private final List<String> recipe;
	private final String hash;

	@JsonCreator
	public Mod(@JsonProperty("id") String id,
	           @JsonProperty("name") String name,
	           @JsonProperty("url") String url,
			   @JsonProperty("hash") String hash,
	           @JsonProperty("recipe") List<String> recipe) {
		this(id, EntryType.MOD, name, url, hash, recipe);
	}

	protected Mod(String id, EntryType type, String name, String url, String hash, List<String> recipe) {
		super(id, type, name);
		this.url = url;
		this.hash = hash;
		this.recipe = recipe != null ? recipe : List.of();
	}

	public boolean hasHash() {
		return hash != null && !hash.isBlank();
	}
}

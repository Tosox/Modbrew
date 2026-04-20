package de.tosox.zonerelay.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Patch extends Mod {
	@JsonCreator
	public Patch(@JsonProperty("id") String id,
	             @JsonProperty("name") String name,
	             @JsonProperty("url") String url,
	             @JsonProperty("hash") String hash,
	             @JsonProperty("setup") List<String> setup) {
		super(id, EntryType.PATCH, name, url, hash, setup);
	}
}

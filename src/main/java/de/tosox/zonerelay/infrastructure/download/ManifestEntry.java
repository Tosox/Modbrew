package de.tosox.zonerelay.infrastructure.download;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ManifestEntry(String url, String filename, String installedHash) {
    @JsonCreator
    public ManifestEntry(
            @JsonProperty("url") String url,
            @JsonProperty("filename") String filename,
            @JsonProperty("installedHash") String installedHash) {
        this.url = url;
        this.filename = filename;
        this.installedHash = installedHash;
    }
}

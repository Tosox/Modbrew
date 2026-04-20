package de.tosox.zonerelay.infrastructure.download;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DownloadManifest {
    private final Map<String, ManifestEntry> mods;

    public DownloadManifest() {
        this.mods = new HashMap<>();
    }

    @JsonCreator
    public DownloadManifest(@JsonProperty("mods") Map<String, ManifestEntry> mods) {
        this.mods = mods != null ? new HashMap<>(mods) : new HashMap<>();
    }

    public ManifestEntry getEntry(String modId) {
        return mods.get(modId);
    }

    public void putEntry(String modId, ManifestEntry entry) {
        mods.put(modId, entry);
    }
}

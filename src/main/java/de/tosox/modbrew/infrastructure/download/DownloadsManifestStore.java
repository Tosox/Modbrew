package de.tosox.modbrew.infrastructure.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.tosox.modbrew.shared.config.AppPaths;
import de.tosox.modbrew.shared.logging.Logger;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class DownloadsManifestStore {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Logger logger;
    private final Path manifestPath;

    @Getter
    private final DownloadManifest manifest;

    @Inject
    public DownloadsManifestStore(@Named("file") Logger logger, AppPaths paths) {
        this.logger = logger;
        this.manifestPath = paths.getDownloadsDir().resolve("manifest.json");
        this.manifest = load();
    }

    public synchronized void recordDownload(String modId, String url, String filename) {
        ManifestEntry existing = manifest.getEntry(modId);
        String installedHash = existing != null ? existing.installedHash() : null;
        manifest.putEntry(modId, new ManifestEntry(url, filename, installedHash));
        persist();
    }

    public synchronized void recordInstall(String modId, String installedHash) {
        ManifestEntry existing = manifest.getEntry(modId);
        if (existing != null) {
            manifest.putEntry(modId, new ManifestEntry(existing.url(), existing.filename(), installedHash));
            persist();
        }
    }

    private DownloadManifest load() {
        if (!Files.exists(manifestPath)) {
            return new DownloadManifest();
        }
        try {
            return MAPPER.readValue(manifestPath.toFile(), DownloadManifest.class);
        } catch (IOException e) {
            logger.warn("Failed to load downloads manifest, starting fresh: %s", e.getMessage());
            return new DownloadManifest();
        }
    }

    private void persist() {
        try {
            Files.createDirectories(manifestPath.getParent());
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(manifestPath.toFile(), manifest);
        } catch (IOException e) {
            logger.error("Failed to persist downloads manifest: %s", e.getMessage());
        }
    }
}

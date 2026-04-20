package de.tosox.zonerelay.domain.port;

import java.io.File;

public record DownloadResult(File archive, String computedHash) {}

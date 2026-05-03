package de.tosox.modbrew.domain.port;

import java.io.File;

public record DownloadResult(File archive, String computedHash) {}

package de.tosox.zonerelay.domain.model;

import java.util.List;

public record SetupFailure(String modName, String modId, List<String> invalidPaths) {}

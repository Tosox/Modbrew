package de.tosox.modbrew.domain.model;

import java.util.List;

public record SetupFailure(String modName, String modId, List<String> invalidPaths) {}

package de.tosox.zonerelay.domain.model;

public record HashMismatch(String modName, String modId, String expectedHash, String actualHash) {}

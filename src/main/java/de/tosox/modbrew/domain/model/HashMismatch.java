package de.tosox.modbrew.domain.model;

public record HashMismatch(String modName, String modId, String expectedHash, String actualHash) {}

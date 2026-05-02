package de.tosox.zonerelay.domain.model;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class SetupPathMissingException extends Exception {
    private final List<String> missingPaths;

    public SetupPathMissingException(List<String> missingPaths) {
        super("Setup paths not found: " + missingPaths);
        this.missingPaths = Collections.unmodifiableList(missingPaths);
    }
}

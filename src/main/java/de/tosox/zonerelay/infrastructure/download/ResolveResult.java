package de.tosox.zonerelay.infrastructure.download;

public record ResolveResult(String url, String hash) {
    public static ResolveResult of(String url) {
        return new ResolveResult(url, null);
    }

    public static ResolveResult of(String url, String hash) {
        return new ResolveResult(url, hash);
    }

    public boolean hasHash() {
        return hash != null && !hash.isBlank();
    }
}

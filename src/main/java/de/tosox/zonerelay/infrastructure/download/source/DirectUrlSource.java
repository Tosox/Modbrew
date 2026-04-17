package de.tosox.zonerelay.infrastructure.download.source;

public class DirectUrlSource implements UrlSource {
	@Override
	public boolean supports(String url) {
		return true; // fallback: supports any URL
	}

	@Override
	public String resolve(String url) {
		return url;
	}
}

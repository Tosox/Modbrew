package de.tosox.zonerelay.infrastructure.download.source;

import de.tosox.zonerelay.infrastructure.download.ResolveResult;

public class DirectUrlSource implements UrlSource {
	@Override
	public boolean supports(String url) {
		return true;
	}

	@Override
	public ResolveResult resolve(String url) {
		return ResolveResult.of(url);
	}
}

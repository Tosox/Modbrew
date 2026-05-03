package de.tosox.modbrew.infrastructure.download.source;

import de.tosox.modbrew.infrastructure.download.ResolveResult;

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

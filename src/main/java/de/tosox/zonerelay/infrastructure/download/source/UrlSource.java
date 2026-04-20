package de.tosox.zonerelay.infrastructure.download.source;

import de.tosox.zonerelay.infrastructure.download.ResolveResult;

public interface UrlSource {
	boolean supports(String url);
	ResolveResult resolve(String url) throws Exception;
}

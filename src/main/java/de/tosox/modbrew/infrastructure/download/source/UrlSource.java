package de.tosox.modbrew.infrastructure.download.source;

import de.tosox.modbrew.infrastructure.download.ResolveResult;

public interface UrlSource {
	boolean supports(String url);
	ResolveResult resolve(String url) throws Exception;
}

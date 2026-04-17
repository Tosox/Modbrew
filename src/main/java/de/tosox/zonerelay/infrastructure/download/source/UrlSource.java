package de.tosox.zonerelay.infrastructure.download.source;

public interface UrlSource {
	boolean supports(String url);
	String resolve(String url) throws Exception;
}

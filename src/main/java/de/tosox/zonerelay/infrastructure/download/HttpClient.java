package de.tosox.zonerelay.infrastructure.download;

import com.google.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Singleton
public class HttpClient {
	private static final String USER_AGENT =
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

	private final OkHttpClient client;

	public HttpClient() {
		this.client = new OkHttpClient.Builder()
				.addInterceptor(chain -> chain.proceed(
						chain.request().newBuilder()
								.header("User-Agent", USER_AGENT)
								.build()
				))
				.build();
	}

	public Response execute(Request request) throws IOException {
		return client.newCall(request).execute();
	}
}

package de.tosox.modbrew.infrastructure.download;

import com.google.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Singleton
public class HttpClient {
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux i686; rv:57.0) Gecko/20100101 Firefox/57.0";

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

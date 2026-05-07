package test;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class HttpTestExecutor {

	private final HttpClient client;
	private final Duration timeout;

	public HttpTestExecutor() {
		this(HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build(), Duration.ofSeconds(20));
	}

	public HttpTestExecutor(HttpClient client, Duration timeout) {
		this.client = client;
		this.timeout = timeout;
	}

	public ExecutedResponse execute(TestRequestSpec spec) throws IOException, InterruptedException {
		String finalUrl = appendQuery(spec.getUrl(), spec.getQueryParams());
		
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(finalUrl))
				.timeout(timeout);

		String method = spec.getMethod() != null ? spec.getMethod().toUpperCase() : "GET";
		switch (method) {
			case "POST":
				builder.POST(HttpRequest.BodyPublishers.noBody());
				break;
			case "PUT":
				builder.PUT(HttpRequest.BodyPublishers.noBody());
				break;
			case "DELETE":
				builder.DELETE();
				break;
			case "HEAD":
				builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
				break;
			default:
				builder.GET();
				break;
		}

		for (Map.Entry<String, String> e : spec.getHeaders().entrySet()) {
			builder.header(e.getKey(), e.getValue());
		}

		long start = System.currentTimeMillis();
		HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		long latency = System.currentTimeMillis() - start;

		return new ExecutedResponse(response.statusCode(), response.body(), latency);
	}

	private String appendQuery(String url, Map<String, String> params) {
		if (url == null || params == null || params.isEmpty()) return url;
		StringBuilder sb = new StringBuilder(url);
		boolean hasQuery = url.contains("?");
		for (Map.Entry<String, String> e : params.entrySet()) {
			sb.append(hasQuery ? '&' : '?');
			hasQuery = true;
			sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
			sb.append('=');
			sb.append(URLEncoder.encode(e.getValue() != null ? e.getValue() : "", StandardCharsets.UTF_8));
		}
		return sb.toString();
	}

	public static final class ExecutedResponse {
		public final int status;
		public final String body;
		public final long latencyMs;

		public ExecutedResponse(int status, String body, long latencyMs) {
			this.status = status;
			this.body = body;
			this.latencyMs = latencyMs;
		}
	}
}

package test;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestRequestSpec {

	private String url;
	private String method = "GET";
	private final Map<String, String> headers = new LinkedHashMap<>();
	private final Map<String, String> queryParams = new LinkedHashMap<>();

	public TestRequestSpec() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, String> getQueryParams() {
		return queryParams;
	}

	public void addHeader(String name, String value) {
		if (name == null || value == null) return;
		headers.put(name, value);
	}

	public void addQueryParam(String key, String value) {
		if (key == null) return;
		queryParams.put(key, value == null ? "" : value);
	}
}

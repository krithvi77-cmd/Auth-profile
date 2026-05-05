package test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRequest {

	private String url;
	private String method = "GET";
	private List<TestParam> params = new ArrayList<>();

	public TestRequest() {
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

	public List<TestParam> getParams() {
		return params;
	}

	public void setParams(List<TestParam> params) {
		this.params = params != null ? params : new ArrayList<>();
	}
}

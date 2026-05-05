package test;

public class TestResult {

	private boolean ok;
	private int status;
	private String message;
	private long latencyMs;
	private String body;
	private boolean needsReconnect;
	private String authType;

	public TestResult() {
	}

	public static TestResult success(int status, String body, long latencyMs) {
		TestResult r = new TestResult();
		r.ok = true;
		r.status = status;
		r.body = body;
		r.latencyMs = latencyMs;
		r.message = "Connection test succeeded (HTTP " + status + ")";
		return r;
	}

	public static TestResult failure(int status, String body, long latencyMs, String message) {
		TestResult r = new TestResult();
		r.ok = false;
		r.status = status;
		r.body = body;
		r.latencyMs = latencyMs;
		r.message = message;
		return r;
	}

	public static TestResult needsReconnect(String message) {
		TestResult r = new TestResult();
		r.ok = false;
		r.needsReconnect = true;
		r.message = message;
		return r;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getLatencyMs() {
		return latencyMs;
	}

	public void setLatencyMs(long latencyMs) {
		this.latencyMs = latencyMs;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isNeedsReconnect() {
		return needsReconnect;
	}

	public void setNeedsReconnect(boolean needsReconnect) {
		this.needsReconnect = needsReconnect;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}
}

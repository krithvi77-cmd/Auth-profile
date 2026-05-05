package test;

import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;

public interface ConnectionTester {

	int authType();

	String authTypeName();

	TestRequestSpec prepare(AuthProfile profile, Connection connection,
			ConnectionOauth oauth, TestRequest request) throws TestPreparationException;

	default TestResult interpret(int status, String responseBody, long latencyMs) {
		if (status == 401 || status == 403) {
			TestResult r = TestResult.failure(status, responseBody, latencyMs,
					"Authentication failed (HTTP " + status + ")");
			r.setNeedsReconnect(true);
			return r;
		}
		if (status >= 200 && status < 300) {
			return TestResult.success(status, responseBody, latencyMs);
		}
		return TestResult.failure(status, responseBody, latencyMs,
				"Connection test failed (HTTP " + status + ")");
	}
}

package test;

import dao.ProfileDAO;
import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;

import java.io.IOException;
import java.sql.SQLException;

public class ConnectionTestService {

	private final ConnectionTestDAO testDAO;
	private final ProfileDAO profileDAO;
	private final HttpTestExecutor executor;

	public ConnectionTestService() {
		this(new ConnectionTestDAO(), new ProfileDAO(), new HttpTestExecutor());
	}

	public ConnectionTestService(ConnectionTestDAO testDAO, ProfileDAO profileDAO, HttpTestExecutor executor) {
		this.testDAO = testDAO;
		this.profileDAO = profileDAO;
		this.executor = executor;
	}

	public TestResult runTest(int connectionId, TestRequest request) throws SQLException {
		if (request == null) {
			return TestResult.failure(0, null, 0, "Request body is required");
		}
		if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
			return TestResult.failure(0, null, 0, "url is required");
		}
		Connection conn = testDAO.loadConnectionWithValues(connectionId);
		if (conn == null) {
			return TestResult.failure(404, null, 0, "Connection not found: id=" + connectionId);
		}
		AuthProfile profile = profileDAO.getByIdUnmasked(conn.getProfileId());
		if (profile == null) {
			return TestResult.failure(404, null, 0,
					"Auth profile not found: id=" + conn.getProfileId());
		}
		ConnectionTester tester = ConnectionTesterRegistry.forAuthType(profile.getAuthType());
		if (tester == null) {
			return TestResult.failure(0, null, 0,
					"No tester registered for auth_type=" + profile.getAuthType());
		}
		ConnectionOauth oauth = null;
		if (profile.getAuthType() == authentication.Oauthv2Authenticator.AUTH_TYPE) {
			oauth = testDAO.loadOauth(connectionId);
		}
		TestRequestSpec spec;
		try {
			spec = tester.prepare(profile, conn, oauth, request);
		} catch (TestPreparationException ex) {
			TestResult r = TestResult.failure(0, null, 0, ex.getMessage());
			r.setNeedsReconnect(ex.isNeedsReconnect());
			r.setAuthType(tester.authTypeName());
			return r;
		}

		try {
			HttpTestExecutor.ExecutedResponse resp = executor.execute(spec);
			TestResult result = tester.interpret(resp.status, resp.body, resp.latencyMs);
			result.setAuthType(tester.authTypeName());
			return result;
		} catch (IOException ex) {
			TestResult r = TestResult.failure(0, null, 0,
					"Network error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			r.setAuthType(tester.authTypeName());
			return r;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			TestResult r = TestResult.failure(0, null, 0, "Test interrupted");
			r.setAuthType(tester.authTypeName());
			return r;
		} catch (IllegalArgumentException ex) {
			TestResult r = TestResult.failure(0, null, 0, "Invalid URL: " + ex.getMessage());
			r.setAuthType(tester.authTypeName());
			return r;
		}
	}
}

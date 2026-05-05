package test;

import authentication.AuthUtil;
import authentication.BasicAuthenticator;
import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class BasicAuthTester implements ConnectionTester {

	@Override
	public int authType() {
		return BasicAuthenticator.AUTH_TYPE;
	}

	@Override
	public String authTypeName() {
		return "basic";
	}

	@Override
	public TestRequestSpec prepare(AuthProfile profile, Connection connection,
			ConnectionOauth oauth, TestRequest request) throws TestPreparationException {

		Map<String, String> values = AuthUtil.toMap(connection);
		String username = values.get("username");
		String password = values.get("password");

		if (username == null || username.isEmpty()) {
			throw new TestPreparationException("username missing on connection " + connection.getId(), true);
		}
		if (password == null) {
			password = "";
		}

		TestRequestSpec spec = new TestRequestSpec();
		spec.setUrl(request.getUrl());
		spec.setMethod(request.getMethod());

		String token = Base64.getEncoder().encodeToString(
				(username + ":" + password).getBytes(StandardCharsets.UTF_8));
		spec.addHeader("Authorization", "Basic " + token);
		spec.addHeader("Accept", "application/json");

		if (request.getParams() != null) {
			for (TestParam p : request.getParams()) {
				if (p == null || p.getKey() == null || p.getKey().isEmpty()) continue;
				spec.addQueryParam(p.getKey(), p.getValue());
			}
		}
		return spec;
	}
}

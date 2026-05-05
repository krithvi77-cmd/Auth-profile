package test;

import authentication.Oauthv2Authenticator;
import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public class OAuthV2Tester implements ConnectionTester {

	@Override
	public int authType() {
		return Oauthv2Authenticator.AUTH_TYPE;
	}

	@Override
	public String authTypeName() {
		return "oauth2";
	}

	@Override
	public TestRequestSpec prepare(AuthProfile profile, Connection connection,
			ConnectionOauth oauth, TestRequest request) throws TestPreparationException {

		if (oauth == null || oauth.getAccessToken() == null || oauth.getAccessToken().isEmpty()) {
			throw new TestPreparationException(
					"OAuth access token missing. Please reconnect.", true);
		}

		if (isExpired(oauth.getExpiresAt())) {
			throw new TestPreparationException(
					"OAuth access token expired. Please reconnect.", true);
		}

		TestRequestSpec spec = new TestRequestSpec();
		spec.setUrl(request.getUrl());
		spec.setMethod(request.getMethod());

		String tokenType = oauth.getTokenType() != null && !oauth.getTokenType().isEmpty()
				? oauth.getTokenType()
				: "Bearer";
		spec.addHeader("Authorization", tokenType + " " + oauth.getAccessToken());
		spec.addHeader("Accept", "application/json");

		if (request.getParams() != null) {
			for (TestParam p : request.getParams()) {
				if (p == null || p.getKey() == null || p.getKey().isEmpty()) continue;
				spec.addQueryParam(p.getKey(), p.getValue());
			}
		}
		return spec;
	}

	private boolean isExpired(String expiresAtIso) {
		if (expiresAtIso == null || expiresAtIso.isEmpty()) return false;
		try {
			Instant expiresAt = Instant.parse(expiresAtIso);
			return expiresAt.isBefore(Instant.now());
		} catch (DateTimeParseException ex) {
			return false;
		}
	}
}

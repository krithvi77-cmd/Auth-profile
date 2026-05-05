package test;

import authentication.ApiKeyAuthenticator;
import authentication.AuthUtil;
import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;
import model.Field;

import java.util.Map;

public class ApiKeyAuthTester implements ConnectionTester {

	@Override
	public int authType() {
		return ApiKeyAuthenticator.AUTH_TYPE;
	}

	@Override
	public String authTypeName() {
		return "apikey";
	}

	@Override
	public TestRequestSpec prepare(AuthProfile profile, Connection connection,
			ConnectionOauth oauth, TestRequest request) throws TestPreparationException {

		Map<String, String> values = AuthUtil.toMap(connection);
		Field target = resolveField(profile, values);
		if (target == null) {
			throw new TestPreparationException("API Key profile has no field defined", true);
		}

		String apiKey = values.get(target.getKey());
		if (apiKey == null) {
			apiKey = values.get("api_key_value");
		}
		if (apiKey == null || apiKey.isEmpty()) {
			throw new TestPreparationException(
					(target.getLabel() != null ? target.getLabel() : target.getKey()) + " is required", true);
		}

		TestRequestSpec spec = new TestRequestSpec();
		spec.setUrl(request.getUrl());
		spec.setMethod(request.getMethod());
		spec.addHeader("Accept", "application/json");

		String placement = target.getPlacement() != null ? target.getPlacement().toLowerCase() : "header";
		String name = target.getKey();
		if (placement.equals("query")) {
			spec.addQueryParam(name, apiKey);
		} else {
			spec.addHeader(name, apiKey);
		}

		if (request.getParams() != null) {
			for (TestParam p : request.getParams()) {
				if (p == null || p.getKey() == null || p.getKey().isEmpty())
					continue;
				spec.addQueryParam(p.getKey(), p.getValue());
			}
		}
		return spec;
	}

	private Field resolveField(AuthProfile profile, Map<String, String> supplied) {
		if (profile.getFields() == null || profile.getFields().isEmpty())
			return null;
		for (Field f : profile.getFields()) {
			if (supplied.containsKey(f.getKey()))
				return f;
		}
		for (Field f : profile.getFields()) {
			if (f.isCustom())
				return f;
		}
		return profile.getFields().get(0);
	}
}

package authentication;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationHandler {

	private static final Map<Integer, Authenticator> REGISTRY = new HashMap<>();

	static {
		register(new BasicAuthenticator());
		register(new Oauthv2Authenticator());
		register(new ApiKeyAuthenticator());
	}

	private AuthenticationHandler() {
	}

	public static void register(Authenticator a) {
		REGISTRY.put(a.authType(), a);
	}

	public static Authenticator forAuthType(int authType) {
		return REGISTRY.get(authType);
	}
}

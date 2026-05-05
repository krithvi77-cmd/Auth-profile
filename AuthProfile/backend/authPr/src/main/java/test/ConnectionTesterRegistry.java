package test;

import java.util.HashMap;
import java.util.Map;

public class ConnectionTesterRegistry {

	private static final Map<Integer, ConnectionTester> REGISTRY = new HashMap<>();

	static {
		register(new BasicAuthTester());
		register(new OAuthV2Tester());
		register(new ApiKeyAuthTester());
	}

	private ConnectionTesterRegistry() {
	}

	public static void register(ConnectionTester tester) {
		REGISTRY.put(tester.authType(), tester);
	}

	public static ConnectionTester forAuthType(int authType) {
		return REGISTRY.get(authType);
	}
}

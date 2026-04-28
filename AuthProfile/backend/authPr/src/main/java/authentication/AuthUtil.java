package authentication;

import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;
import model.ConnectionValue;
import model.Field;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class AuthUtil {

	private AuthUtil() {
	}


	public static void insertValue(java.sql.Connection jdbc, int connectionId, int fieldId, String key, String value)
			throws SQLException {
		String sql = "INSERT INTO connection_values (connection_id, field_id, `key`, value) " +
				"VALUES (?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE `key` = VALUES(`key`), value = VALUES(value)";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			ps.setInt(2, fieldId);
			if (key != null) {
				ps.setString(3, key);
			} else {
				ps.setNull(3, Types.VARCHAR);
			}
			if (value != null) {
				ps.setString(4, value);
			} else {
				ps.setNull(4, Types.VARCHAR);
			}
			ps.executeUpdate();
		}
	}

	public static Field findField(AuthProfile profile, String key) {
		if (profile.getFields() == null) {
			return null;
		}
		for (Field f : profile.getFields()) {
			if (key.equalsIgnoreCase(f.getKey())) {
				return f;
			}
		}
		return null;
	}

	public static Map<String, String> toMap(Connection conn) {
		Map<String, String> m = new HashMap<>();
		if (conn.getFields() != null) {
			for (ConnectionValue v : conn.getFields()) {
				if (v.getKey() != null) {
					m.put(v.getKey(), v.getValue());
				}
			}
		}
		return m;
	}


	static ConnectionOauth buildOauth(Connection conn) {
		Map<String, String> m = toMap(conn);
		ConnectionOauth o = new ConnectionOauth();
		o.setAccessToken(firstNonNull(m.get("access_token"), m.get("accessToken")));
		o.setRefreshToken(firstNonNull(m.get("refresh_token"), m.get("refreshToken")));
		o.setExpiresAt(firstNonNull(m.get("expires_at"), m.get("expiresAt")));
		String tokenType = firstNonNull(m.get("token_type"), m.get("tokenType"));
		if (tokenType != null) o.setTokenType(tokenType);
		o.setScope(m.get("scope"));
		return o;
	}

	private static String firstNonNull(String a, String b) {
		return a != null ? a : b;
	}
}

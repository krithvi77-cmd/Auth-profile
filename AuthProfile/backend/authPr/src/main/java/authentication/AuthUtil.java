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

	public static int insertValue(java.sql.Connection jdbc, int fieldId, String key, String value)
			throws SQLException {
		String sql = "INSERT INTO connection_values (field_id, `key`, value) VALUES (?, ?, ?)";
		try (PreparedStatement ps = jdbc.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, fieldId);
			if (key != null) {
				ps.setString(2, key);
			} else {
				ps.setNull(2, Types.VARCHAR);
			}
			if (value != null) {
				ps.setString(3, value);
			} else {
				ps.setNull(3, Types.VARCHAR);
			}
			ps.executeUpdate();

			try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
				if (!keys.next()) {
					throw new SQLException("Insert connection_values failed, no id returned");
				}
				return keys.getInt(1);
			}
		}
	}

	public static int insertValueAsJson(java.sql.Connection jdbc, int fieldId, String key,
			Map<String, String> valuesMap) throws SQLException {
		String jsonValue = mapToJson(valuesMap);
		String sql = "INSERT INTO connection_values (field_id, `key`, value) VALUES (?, ?, ?)";
		try (PreparedStatement ps = jdbc.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, fieldId);
			if (key != null) {
				ps.setString(2, key);
			} else {
				ps.setNull(2, Types.VARCHAR);
			}
			ps.setString(3, jsonValue);
			ps.executeUpdate();

			try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
				if (!keys.next()) {
					throw new SQLException("Insert connection_values failed, no id returned");
				}
				return keys.getInt(1);
			}
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

	private static String mapToJson(Map<String, String> map) {
		StringBuilder sb = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<String, String> e : map.entrySet()) {
			if (!first) sb.append(",");
			first = false;
			sb.append("\"").append(escapeJson(e.getKey())).append("\":");
			if (e.getValue() == null) {
				sb.append("null");
			} else {
				sb.append("\"").append(escapeJson(e.getValue())).append("\"");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	private static String escapeJson(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}

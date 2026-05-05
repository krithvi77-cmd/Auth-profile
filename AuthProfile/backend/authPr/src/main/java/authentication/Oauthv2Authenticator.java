package authentication;

import model.AuthProfile;
import model.Connection;
import model.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Oauthv2Authenticator implements Authenticator {

	public static final int AUTH_TYPE = 2;

	public static final String F_CLIENT_ID         = "client_id";
	public static final String F_CLIENT_SECRET     = "client_secret";
	public static final String F_AUTHORIZATION_URL = "authorization_url";
	public static final String F_ACCESS_TOKEN_URL  = "access_token_url";
	public static final String F_SCOPES            = "scopes";
	public static final String F_REDIRECT_URI      = "redirect_uri";

	@Override
	public int authType() {
		return AUTH_TYPE;
	}

	@Override
	public String validate(AuthProfile profile, Connection conn) {
		if (conn.getName() == null || conn.getName().trim().isEmpty()) {
			return "Connection name is required";
		}
		Map<String, String> cfg = profileFieldValues(profile);
		if (isBlank(cfg.get(F_CLIENT_ID)))         return "client_id is not configured on the auth profile";
		if (isBlank(cfg.get(F_CLIENT_SECRET)))     return "client_secret is not configured on the auth profile";
		if (isBlank(cfg.get(F_AUTHORIZATION_URL))) return "authorization_url is not configured on the auth profile";
		if (isBlank(cfg.get(F_ACCESS_TOKEN_URL)))  return "access_token_url is not configured on the auth profile";
		return null;
	}

	@Override
	public int save(java.sql.Connection jdbc, AuthProfile profile, Connection conn) throws SQLException {
		int oauthRowId = insertOauthPlaceholder(jdbc);

		int connectionId = insertConnection(jdbc, profile, conn, oauthRowId);

		updateOauthConnectionId(jdbc, oauthRowId, connectionId);

		conn.setValueType(Connection.VALUE_TYPE_OAUTH);
		conn.setValueId(oauthRowId);
		conn.setStatus("inactive");
		return connectionId;
	}

	private int insertOauthPlaceholder(java.sql.Connection jdbc) throws SQLException {
		String sql = "INSERT INTO connection_oauth_values "
				+ "(connection_id, access_token, refresh_token, expires_at) "
				+ "VALUES (0, NULL, NULL, NULL)";
		try (PreparedStatement ps = jdbc.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (!keys.next()) {
					throw new SQLException("Insert connection_oauth_values failed, no id returned");
				}
				return keys.getInt(1);
			}
		}
	}

	private void updateOauthConnectionId(java.sql.Connection jdbc, int oauthRowId, int connectionId)
			throws SQLException {
		String sql = "UPDATE connection_oauth_values SET connection_id = ? WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			ps.setInt(2, oauthRowId);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				throw new SQLException("connection_oauth_values row " + oauthRowId
						+ " not found when assigning connection_id");
			}
		}
	}

	private int insertConnection(java.sql.Connection jdbc, AuthProfile profile, Connection conn, int oauthRowId)
			throws SQLException {
		String sql = "INSERT INTO connections (profile_id, user_id, name, status, value_type, value_id) "
				+ "VALUES (?, ?, ?, 'inactive', ?, ?)";
		try (PreparedStatement ps = jdbc.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, profile.getId());
			int userId = conn.getUserId() != null ? conn.getUserId()
					: (profile.getCreatedBy() != null ? profile.getCreatedBy() : 0);
			ps.setInt(2, userId);
			ps.setString(3, conn.getName().trim());
			ps.setString(4, Connection.VALUE_TYPE_OAUTH);
			ps.setInt(5, oauthRowId);
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (!keys.next()) throw new SQLException("Insert connections failed, no id returned");
				return keys.getInt(1);
			}
		}
	}

	public static Map<String, String> profileFieldValues(AuthProfile profile) {
		Map<String, String> m = new HashMap<>();
		List<Field> fields = profile.getFields();
		if (fields == null) return m;
		for (Field f : fields) {
			if (f.getKey() != null) {
				m.put(f.getKey(), f.getDefaultValue());
			}
		}
		return m;
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}

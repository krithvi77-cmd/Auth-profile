package dao;

import authentication.AuthenticationHandler;
import authentication.Authenticator;
import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;
import model.ConnectionValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionDAO {

	public int create(AuthProfile profile, Connection conn) throws SQLException {
		Authenticator authenticator = AuthenticationHandler.forAuthType(profile.getAuthType());
		if (authenticator == null) {
			throw new IllegalArgumentException(
					"No authenticator registered for auth_type=" + profile.getAuthType());
		}

		String err = authenticator.validate(profile, conn);
		if (err != null) {
			throw new IllegalArgumentException(err);
		}

		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				int id = authenticator.save(jdbc, profile, conn);
				jdbc.commit();
				conn.setId(id);
				conn.setProfileId(profile.getId());
				return id;
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	public void update(AuthProfile profile, Connection conn) throws SQLException {
		if (conn.getId() <= 0) {
			throw new IllegalArgumentException("Connection id is required for update");
		}

		Authenticator authenticator = AuthenticationHandler.forAuthType(profile.getAuthType());
		if (authenticator == null) {
			throw new IllegalArgumentException(
					"No authenticator registered for auth_type=" + profile.getAuthType());
		}

		String err = authenticator.validate(profile, conn);
		if (err != null) {
			throw new IllegalArgumentException(err);
		}

		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				assertProfileUnchanged(jdbc, conn.getId(), profile.getId());
				updateConnectionRow(jdbc, profile, conn);
				updateCredentials(jdbc, profile, conn);
				jdbc.commit();
				conn.setProfileId(profile.getId());
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	private void assertProfileUnchanged(java.sql.Connection jdbc, int connectionId, int newProfileId)
			throws SQLException {
		try (PreparedStatement ps = jdbc.prepareStatement(
				"SELECT profile_id FROM connections WHERE id = ?")) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Connection " + connectionId + " not found");
				}
				int currentProfileId = rs.getInt("profile_id");
				if (currentProfileId != newProfileId) {
					throw new IllegalArgumentException(
							"Cannot change profile on an existing connection (current="
									+ currentProfileId + ", requested=" + newProfileId
									+ "). Delete and recreate the connection instead.");
				}
			}
		}
	}

	private void updateConnectionRow(java.sql.Connection jdbc, AuthProfile profile, Connection conn)
			throws SQLException {
		boolean isOauth = profile.getAuthType() == authentication.Oauthv2Authenticator.AUTH_TYPE;
		String newStatus = isOauth ? "inactive"
				: (conn.getStatus() != null ? conn.getStatus() : "active");

		String sql = "UPDATE connections SET name = ?, status = ? WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setString(1, conn.getName().trim());
			ps.setString(2, newStatus);
			ps.setInt(3, conn.getId());
			int affected = ps.executeUpdate();
			if (affected == 0) {
				throw new SQLException("Connection " + conn.getId() + " not found for update");
			}
		}
		conn.setStatus(newStatus);
	}

	private void updateCredentials(java.sql.Connection jdbc, AuthProfile profile, Connection conn)
			throws SQLException {
		int authType = profile.getAuthType();
		Map<String, String> supplied = authentication.AuthUtil.toMap(conn);
		int connectionId = conn.getId();

		if (authType == authentication.BasicAuthenticator.AUTH_TYPE) {
			model.Field userField = authentication.AuthUtil.findField(profile, "username");
			model.Field passField = authentication.AuthUtil.findField(profile, "password");

			updateValue(jdbc, connectionId, userField.getId(), "username", supplied.get("username"));
			updateValue(jdbc, connectionId, passField.getId(), "password", supplied.get("password"));
			return;
		}

		if (authType == authentication.ApiKeyAuthenticator.AUTH_TYPE) {
			model.Field target = null;
			if (profile.getFields() != null) {
				for (model.Field f : profile.getFields()) {
					if (supplied.containsKey(f.getKey())) { target = f; break; }
				}
				if (target == null) {
					for (model.Field f : profile.getFields()) {
						if (f.isCustom()) { target = f; break; }
					}
				}
				if (target == null && !profile.getFields().isEmpty()) {
					target = profile.getFields().get(0);
				}
			}
			if (target == null) {
				throw new SQLException("API Key profile has no field defined");
			}
			String value = supplied.get(target.getKey());
			if (value == null) value = supplied.get("api_key_value");

			updateValue(jdbc, connectionId, target.getId(), target.getKey(), value);
			return;
		}
	}

	private void updateValue(java.sql.Connection jdbc, int connectionId, int fieldId,
			String key, String value) throws SQLException {
		String sql = "UPDATE connection_values SET `key` = ?, value = ? "
				+ "WHERE connection_id = ? AND field_id = ?";
		int affected;
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			if (key != null) ps.setString(1, key); else ps.setNull(1, Types.VARCHAR);
			if (value != null) ps.setString(2, value); else ps.setNull(2, Types.VARCHAR);
			ps.setInt(3, connectionId);
			ps.setInt(4, fieldId);
			affected = ps.executeUpdate();
		}
		if (affected == 0) {
			authentication.AuthUtil.insertValue(jdbc, connectionId, fieldId, key, value);
		}
	}

	public static void linkValues(java.sql.Connection jdbc, int connectionId, int connectionValuesRowId)
			throws SQLException {
		linkValueRef(jdbc, connectionId, Connection.VALUE_TYPE_VALUES, connectionValuesRowId);
	}

	public static void linkOauth(java.sql.Connection jdbc, int connectionId, int connectionOauthRowId)
			throws SQLException {
		linkValueRef(jdbc, connectionId, Connection.VALUE_TYPE_OAUTH, connectionOauthRowId);
	}

	private static void linkValueRef(java.sql.Connection jdbc, int connectionId,
			String valueType, int valueId) throws SQLException {
		String sql = "UPDATE connections SET value_type = ?, value_id = ? WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setString(1, valueType);
			ps.setInt(2, valueId);
			ps.setInt(3, connectionId);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				throw new SQLException("Connection " + connectionId + " not found when linking value reference");
			}
		}
	}

	public List<Connection> list() throws SQLException {
		String sql =
				"SELECT c.id, c.profile_id, c.user_id, c.name, c.status, c.created_at, "
				+ "     c.value_type, c.value_id, "
				+ "     p.name AS profile_name, p.auth_type "
				+ "FROM connections c "
				+ "LEFT JOIN profiles p ON p.id = c.profile_id "
				+ "ORDER BY c.created_at DESC";

		List<Connection> list = new ArrayList<>();
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(mapRow(rs));
			}
		}
		return list;
	}

	public Connection getById(int id) throws SQLException {
		String sql =
				"SELECT c.id, c.profile_id, c.user_id, c.name, c.status, c.created_at, "
				+ "     c.value_type, c.value_id, "
				+ "     p.name AS profile_name, p.auth_type "
				+ "FROM connections c "
				+ "LEFT JOIN profiles p ON p.id = c.profile_id "
				+ "WHERE c.id = ?";

		Connection conn;
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				conn = mapRow(rs);
			}
		}

		if (conn.isValuesType()) {
			conn.setFields(loadValuesByConnectionId(conn.getId()));
		} else if (conn.isOauthType()) {
			conn.setOauthData(loadOauthByConnectionId(conn.getId()));
		}

		return conn;
	}

	public Connection getByIdShallow(int id) throws SQLException {
		String sql =
				"SELECT c.id, c.profile_id, c.user_id, c.name, c.status, c.created_at, "
				+ "     c.value_type, c.value_id, "
				+ "     p.name AS profile_name, p.auth_type "
				+ "FROM connections c "
				+ "LEFT JOIN profiles p ON p.id = c.profile_id "
				+ "WHERE c.id = ?";

		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				return mapRow(rs);
			}
		}
	}

	public boolean delete(int id) throws SQLException {
		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				String valueType = null;
				try (PreparedStatement ps = jdbc.prepareStatement(
						"SELECT value_type FROM connections WHERE id = ?")) {
					ps.setInt(1, id);
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next()) {
							jdbc.rollback();
							return false;
						}
						valueType = rs.getString("value_type");
					}
				}

				if (Connection.VALUE_TYPE_VALUES.equalsIgnoreCase(valueType)) {
					try (PreparedStatement ps = jdbc.prepareStatement(
							"DELETE FROM connection_values WHERE connection_id = ?")) {
						ps.setInt(1, id);
						ps.executeUpdate();
					}
				} else if (Connection.VALUE_TYPE_OAUTH.equalsIgnoreCase(valueType)) {
					try (PreparedStatement ps = jdbc.prepareStatement(
							"DELETE FROM connection_oauth_values WHERE connection_id = ?")) {
						ps.setInt(1, id);
						ps.executeUpdate();
					}
				}

				int affected;
				try (PreparedStatement ps = jdbc.prepareStatement(
						"DELETE FROM connections WHERE id = ?")) {
					ps.setInt(1, id);
					affected = ps.executeUpdate();
				}

				jdbc.commit();
				return affected > 0;
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	public boolean reconnect(AuthProfile profile, int connectionId, Map<String, String> newValues)
			throws SQLException {
		if (connectionId <= 0) {
			throw new IllegalArgumentException("connectionId is required");
		}
		if (profile == null) {
			throw new IllegalArgumentException("profile is required");
		}

		if (profile.getAuthType() == authentication.Oauthv2Authenticator.AUTH_TYPE) {
			throw new IllegalArgumentException(
					"OAuth reconnect must be performed via the authorize URL, not the values endpoint");
		}

		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				int storedProfileId;
				try (PreparedStatement ps = jdbc.prepareStatement(
						"SELECT profile_id FROM connections WHERE id = ?")) {
					ps.setInt(1, connectionId);
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next()) {
							jdbc.rollback();
							return false;
						}
						storedProfileId = rs.getInt(1);
					}
				}
				if (storedProfileId != profile.getId()) {
					throw new IllegalStateException(
							"Profile mismatch on reconnect: connection " + connectionId
									+ " is bound to profile " + storedProfileId
									+ " but caller passed " + profile.getId());
				}

				if (newValues != null && !newValues.isEmpty() && profile.getFields() != null) {
					for (Map.Entry<String, String> e : newValues.entrySet()) {
						if (e.getKey() == null) continue;
						model.Field target = authentication.AuthUtil.findField(profile, e.getKey());
						if (target == null) continue;
						updateValue(jdbc, connectionId, target.getId(), e.getKey(), e.getValue());
					}
				}

				try (PreparedStatement ps = jdbc.prepareStatement(
						"UPDATE connections SET status = 'active' WHERE id = ?")) {
					ps.setInt(1, connectionId);
					ps.executeUpdate();
				}

				jdbc.commit();
				return true;
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	private List<ConnectionValue> loadValuesByConnectionId(int connectionId) throws SQLException {
		String sql = "SELECT id, connection_id, field_id, `key`, value "
				+ "FROM connection_values WHERE connection_id = ?";
		List<ConnectionValue> list = new ArrayList<>();
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ConnectionValue v = new ConnectionValue();
					v.setId(rs.getInt("id"));
					v.setConnectionId(rs.getInt("connection_id"));
					v.setFieldId(rs.getInt("field_id"));
					v.setKey(rs.getString("key"));
					v.setValue(rs.getString("value"));
					list.add(v);
				}
			}
		}
		return list;
	}

	private ConnectionOauth loadOauthByConnectionId(int connectionId) throws SQLException {
		String sql = "SELECT id, connection_id, access_token, refresh_token, expires_at "
				+ "FROM connection_oauth_values WHERE connection_id = ?";
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				ConnectionOauth o = new ConnectionOauth();
				o.setId(rs.getInt("id"));
				o.setConnectionId(rs.getInt("connection_id"));
				o.setAccessToken(rs.getString("access_token"));
				o.setRefreshToken(rs.getString("refresh_token"));
				Timestamp ts = rs.getTimestamp("expires_at");
				if (ts != null) o.setExpiresAt(ts.toInstant().toString());
				return o;
			}
		}
	}

	private Connection mapRow(ResultSet rs) throws SQLException {
		Connection c = new Connection();
		c.setId(rs.getInt("id"));
		c.setProfileId(rs.getInt("profile_id"));

		int uid = rs.getInt("user_id");
		c.setUserId(rs.wasNull() ? null : uid);

		c.setName(rs.getString("name"));
		c.setStatus(rs.getString("status"));

		c.setValueType(rs.getString("value_type"));
		int vid = rs.getInt("value_id");
		c.setValueId(rs.wasNull() ? null : vid);

		Timestamp ts = rs.getTimestamp("created_at");
		if (ts != null) c.setCreatedAt(ts.toString());
		return c;
	}
}

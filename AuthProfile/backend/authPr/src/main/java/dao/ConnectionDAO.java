package dao;

import authentication.AuthenticationHandler;
import authentication.Authenticator;
import model.AuthProfile;
import model.Connection;
import model.ConnectionOauth;
import model.ConnectionValue;
import model.Field;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionDAO {

	public int create(AuthProfile profile, Connection conn) throws SQLException {
		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				Authenticator auth = AuthenticationHandler.forAuthType(profile.getAuthType());
				if (auth == null) {
					throw new SQLException("No authenticator for auth_type=" + profile.getAuthType());
				}
				int connectionId = auth.save(jdbc, profile, conn);
				jdbc.commit();
				return connectionId;
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	public Connection getById(int connectionId) throws SQLException {
		String sql = "SELECT id, profile_id, user_id, name, status, created_at, "
				+ "value_type, value_id "
				+ "FROM connections WHERE id = ?";
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				Connection conn = mapRow(rs);
				conn.setValueType(rs.getString("value_type"));
				int vid = rs.getInt("value_id");
				conn.setValueId(rs.wasNull() ? null : vid);
				loadValues(jdbc, conn);
				loadOauth(jdbc, conn);
				return conn;
			}
		}
	}

	public Connection getByIdShallow(int connectionId) throws SQLException {
		String sql = "SELECT id, profile_id, user_id, name, status, created_at, "
				+ "value_type, value_id "
				+ "FROM connections WHERE id = ?";
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				Connection conn = mapRow(rs);
				conn.setValueType(rs.getString("value_type"));
				int vid = rs.getInt("value_id");
				conn.setValueId(rs.wasNull() ? null : vid);
				return conn;
			}
		}
	}

	public List<Connection> list() throws SQLException {
		String sql = "SELECT id, profile_id, user_id, name, status, created_at, "
				+ "value_type, value_id "
				+ "FROM connections ORDER BY id";
		List<Connection> list = new ArrayList<>();
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Connection conn = mapRow(rs);
				conn.setValueType(rs.getString("value_type"));
				int vid = rs.getInt("value_id");
				conn.setValueId(rs.wasNull() ? null : vid);
				loadValues(jdbc, conn);
				loadOauth(jdbc, conn);
				list.add(conn);
			}
		}
		return list;
	}

	public List<Connection> getByProfileId(int profileId) throws SQLException {
		String sql = "SELECT id, profile_id, user_id, name, status, created_at, "
				+ "value_type, value_id "
				+ "FROM connections WHERE profile_id = ? ORDER BY id";
		List<Connection> list = new ArrayList<>();
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, profileId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Connection conn = mapRow(rs);
					conn.setValueType(rs.getString("value_type"));
					int vid = rs.getInt("value_id");
					conn.setValueId(rs.wasNull() ? null : vid);
					loadValues(jdbc, conn);
					loadOauth(jdbc, conn);
					list.add(conn);
				}
			}
		}
		return list;
	}

	public void update(AuthProfile profile, Connection conn) throws SQLException {
		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				String sql = "UPDATE connections SET name = ?, status = ? WHERE id = ?";
				try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
					ps.setString(1, conn.getName() != null ? conn.getName().trim() : null);
					ps.setString(2, conn.getStatus() != null ? conn.getStatus() : "active");
					ps.setInt(3, conn.getId());
					ps.executeUpdate();
				}

				if (profile.getAuthType() != 2) {
					String[] ref = lookupValueRef(jdbc, conn.getId());
					int valueId = Integer.parseInt(ref[1]);
					if (valueId > 0) {
						updateConnectionValueRef(jdbc, conn.getId(), null, 0);
						deleteRowById(jdbc, ref[0], valueId);
					}
					insertValueRowsAndLink(jdbc, conn.getId(), profile, conn);
				}

				jdbc.commit();
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	public void reconnect(AuthProfile profile, int connectionId, Map<String, String> newValues)
			throws SQLException {
		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				String[] ref = lookupValueRef(jdbc, connectionId);
				int oldValueId = Integer.parseInt(ref[1]);
				if (oldValueId > 0) {
					updateConnectionValueRef(jdbc, connectionId, null, 0);
					deleteRowById(jdbc, ref[0], oldValueId);
				}

				if (newValues != null && !newValues.isEmpty()) {
					int newValueId = insertSingleValueRow(jdbc, newValues, profile);
					updateConnectionValueRef(jdbc, connectionId, Connection.VALUE_TYPE_VALUES, newValueId);
				}

				try (PreparedStatement ps = jdbc.prepareStatement(
						"UPDATE connections SET status = 'active' WHERE id = ?")) {
					ps.setInt(1, connectionId);
					ps.executeUpdate();
				}

				jdbc.commit();
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	public boolean delete(int connectionId) throws SQLException {
		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			jdbc.setAutoCommit(false);
			try {
				String vt = null;
				int vid = 0;
				try (PreparedStatement ps = jdbc.prepareStatement(
						"SELECT value_type, value_id FROM connections WHERE id = ?")) {
					ps.setInt(1, connectionId);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							vt = rs.getString("value_type");
							vid = rs.getInt("value_id");
						}
					}
				}
				int rows;
				try (PreparedStatement ps = jdbc.prepareStatement(
						"DELETE FROM connections WHERE id = ?")) {
					ps.setInt(1, connectionId);
					rows = ps.executeUpdate();
				}
				deleteRowById(jdbc, vt, vid);
				jdbc.commit();
				return rows > 0;
			} catch (SQLException ex) {
				jdbc.rollback();
				throw ex;
			} finally {
				jdbc.setAutoCommit(true);
			}
		}
	}

	public void deleteByProfileId(int profileId) throws SQLException {
		List<Connection> connections = getByProfileId(profileId);
		for (Connection conn : connections) {
			delete(conn.getId());
		}
	}

	private Connection mapRow(ResultSet rs) throws SQLException {
		Connection conn = new Connection();
		conn.setId(rs.getInt("id"));
		conn.setProfileId(rs.getInt("profile_id"));
		conn.setUserId(rs.getInt("user_id"));
		conn.setName(rs.getString("name"));
		conn.setStatus(rs.getString("status"));
		conn.setCreatedAt(rs.getString("created_at"));
		return conn;
	}

	private void loadValues(java.sql.Connection jdbc, Connection conn) throws SQLException {
		if (!Connection.VALUE_TYPE_VALUES.equals(conn.getValueType())) return;
		if (conn.getValueId() == null || conn.getValueId() <= 0) return;

		String sql = "SELECT id, field_id, `key`, value, created_at "
				+ "FROM connection_values WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, conn.getValueId());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					List<ConnectionValue> fields = new ArrayList<>();
					String rawValue = rs.getString("value");
					if (rawValue != null && rawValue.startsWith("{")) {
						parseJsonValues(rawValue, fields);
					} else {
						ConnectionValue cv = new ConnectionValue();
						cv.setId(rs.getInt("id"));
						cv.setFieldId(rs.getInt("field_id"));
						cv.setKey(rs.getString("key"));
						cv.setValue(rawValue);
						cv.setCreatedAt(rs.getString("created_at"));
						fields.add(cv);
					}
					conn.setFields(fields);
				}
			}
		}
	}

	private void loadOauth(java.sql.Connection jdbc, Connection conn) throws SQLException {
		if (!Connection.VALUE_TYPE_OAUTH.equals(conn.getValueType())) return;
		if (conn.getValueId() == null || conn.getValueId() <= 0) return;

		String sql = "SELECT id, access_token, refresh_token, expires_at, created_at "
				+ "FROM connection_oauth_values WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, conn.getValueId());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					ConnectionOauth oauth = new ConnectionOauth();
					oauth.setId(rs.getInt("id"));
					oauth.setAccessToken(rs.getString("access_token"));
					oauth.setRefreshToken(rs.getString("refresh_token"));
					oauth.setExpiresAt(rs.getString("expires_at"));
					oauth.setCreatedAt(rs.getString("created_at"));
					conn.setOauthData(oauth);
				}
			}
		}
	}

	private void insertValueRowsAndLink(java.sql.Connection jdbc, int connectionId,
			AuthProfile profile, Connection conn) throws SQLException {
		List<ConnectionValue> fields = conn.getFields();
		if (fields == null || fields.isEmpty()) return;

		Map<String, String> valuesMap = new java.util.LinkedHashMap<>();
		for (ConnectionValue cv : fields) {
			if (cv.getKey() != null) {
				valuesMap.put(cv.getKey(), cv.getValue());
			}
		}

		int newValueId = insertSingleValueRow(jdbc, valuesMap, profile);
		updateConnectionValueRef(jdbc, connectionId, Connection.VALUE_TYPE_VALUES, newValueId);
	}

	private int insertSingleValueRow(java.sql.Connection jdbc, Map<String, String> valuesMap,
			AuthProfile profile) throws SQLException {
		String jsonValue = mapToJson(valuesMap);
		int fieldId = 0;
		String firstKey = null;
		if (!valuesMap.isEmpty()) {
			firstKey = valuesMap.keySet().iterator().next();
			Field f = findField(profile, firstKey);
			if (f != null) fieldId = f.getId();
		}

		String sql = "INSERT INTO connection_values (field_id, `key`, value) VALUES (?, ?, ?)";
		try (PreparedStatement ps = jdbc.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, fieldId);
			if (firstKey != null) {
				ps.setString(2, firstKey);
			} else {
				ps.setNull(2, Types.VARCHAR);
			}
			ps.setString(3, jsonValue);
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (!keys.next()) throw new SQLException("Insert connection_values failed, no id returned");
				return keys.getInt(1);
			}
		}
	}

	private void updateConnectionValueRef(java.sql.Connection jdbc, int connectionId,
			String valueType, int valueId) throws SQLException {
		String sql = "UPDATE connections SET value_type = ?, value_id = ? WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setString(1, valueType);
			ps.setInt(2, valueId);
			ps.setInt(3, connectionId);
			ps.executeUpdate();
		}
	}

	private int lookupValueId(java.sql.Connection jdbc, int connectionId) throws SQLException {
		String sql = "SELECT value_id FROM connections WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getInt("value_id");
				return 0;
			}
		}
	}

	private String[] lookupValueRef(java.sql.Connection jdbc, int connectionId) throws SQLException {
		String sql = "SELECT value_type, value_id FROM connections WHERE id = ?";
		try (PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new String[] { rs.getString("value_type"), String.valueOf(rs.getInt("value_id")) };
				}
				return new String[] { null, "0" };
			}
		}
	}

	private void deleteRowById(java.sql.Connection jdbc, String valueType, int valueId) throws SQLException {
		if (valueType == null || valueId <= 0) return;		
		try (PreparedStatement ps = jdbc.prepareStatement(
				"SELECT COUNT(*) FROM connections WHERE value_type = ? AND value_id = ?")) {
			ps.setString(1, valueType);
			ps.setInt(2, valueId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					return; 
				}
			}
		}
		String tableName = Connection.VALUE_TYPE_OAUTH.equalsIgnoreCase(valueType)
				? "connection_oauth_values"
				: "connection_values";
		try (PreparedStatement ps = jdbc.prepareStatement(
				"DELETE FROM " + tableName + " WHERE id = ?")) {
			ps.setInt(1, valueId);
			ps.executeUpdate();
		}
	}

	private void parseJsonValues(String json, List<ConnectionValue> fields) {
		String trimmed = json.trim();
		if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
		if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);
		if (trimmed.isEmpty()) return;

		String[] pairs = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
		for (String pair : pairs) {
			int colon = pair.indexOf(':');
			if (colon < 0) continue;
			String k = pair.substring(0, colon).trim();
			String v = pair.substring(colon + 1).trim();
			if (k.startsWith("\"") && k.endsWith("\"")) k = k.substring(1, k.length() - 1);
			if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
			if ("null".equals(v)) v = null;
			ConnectionValue cv = new ConnectionValue(k, v);
			fields.add(cv);
		}
	}

	private String mapToJson(Map<String, String> map) {
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

	private String escapeJson(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private Field findField(AuthProfile profile, String key) {
		if (profile.getFields() == null || key == null) return null;
		for (Field f : profile.getFields()) {
			if (key.equalsIgnoreCase(f.getKey())) {
				return f;
			}
		}
		return null;
	}
}

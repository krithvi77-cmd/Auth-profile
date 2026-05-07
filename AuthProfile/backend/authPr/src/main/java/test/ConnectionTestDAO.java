package test;

import dao.DBUtil;
import model.Connection;
import model.ConnectionOauth;
import model.ConnectionValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ConnectionTestDAO {

	public Connection loadConnectionWithValues(int connectionId) throws SQLException {
		String connSql = "SELECT id, profile_id, user_id, name, status, created_at, "
				+ "value_type, value_id "
				+ "FROM connections WHERE id = ?";
		Connection conn;
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(connSql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				conn = new Connection();
				conn.setId(rs.getInt("id"));
				conn.setProfileId(rs.getInt("profile_id"));
				int uid = rs.getInt("user_id");
				conn.setUserId(rs.wasNull() ? null : uid);
				conn.setName(rs.getString("name"));
				conn.setStatus(rs.getString("status"));
				conn.setValueType(rs.getString("value_type"));
				int vid = rs.getInt("value_id");
				conn.setValueId(rs.wasNull() ? null : vid);
				Timestamp ts = rs.getTimestamp("created_at");
				if (ts != null) conn.setCreatedAt(ts.toString());
			}
		}

		if (Connection.VALUE_TYPE_VALUES.equals(conn.getValueType())
				&& conn.getValueId() != null && conn.getValueId() > 0) {
			List<ConnectionValue> values = loadValues(conn.getValueId());
			conn.setFields(values);
		}
		return conn;
	}

	private List<ConnectionValue> loadValues(int valueId) throws SQLException {
		String sql = "SELECT id, field_id, `key`, value "
				+ "FROM connection_values WHERE id = ?";
		List<ConnectionValue> list = new ArrayList<>();
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, valueId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String rawValue = rs.getString("value");
					if (rawValue != null && rawValue.startsWith("{")) {
						parseJsonValues(rawValue, list);
					} else {
						ConnectionValue v = new ConnectionValue();
						v.setId(rs.getInt("id"));
						v.setFieldId(rs.getInt("field_id"));
						v.setKey(rs.getString("key"));
						v.setValue(rawValue);
						list.add(v);
					}
				}
			}
		}
		return list;
	}

	public ConnectionOauth loadOauth(int connectionId) throws SQLException {
		String sql = "SELECT ov.access_token, ov.refresh_token, ov.expires_at "
				+ "FROM connection_oauth_values ov "
				+ "INNER JOIN connections c ON c.value_id = ov.id "
				+ "WHERE c.id = ? AND c.value_type = 'OAUTH'";
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				ConnectionOauth o = new ConnectionOauth();
				o.setAccessToken(rs.getString("access_token"));
				o.setRefreshToken(rs.getString("refresh_token"));
				Timestamp ts = rs.getTimestamp("expires_at");
				if (ts != null) o.setExpiresAt(ts.toInstant().toString());
				return o;
			}
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
}

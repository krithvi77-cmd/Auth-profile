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

		List<ConnectionValue> values = loadValues(connectionId);
		conn.setFields(values);
		return conn;
	}

	private List<ConnectionValue> loadValues(int connectionId) throws SQLException {
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

	public ConnectionOauth loadOauth(int connectionId) throws SQLException {
		String sql = "SELECT access_token, refresh_token, expires_at "
				+ "FROM connection_oauth_values WHERE connection_id = ?";
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(sql)) {
			ps.setInt(1, connectionId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				ConnectionOauth o = new ConnectionOauth();
				o.setConnectionId(connectionId);
				o.setAccessToken(rs.getString("access_token"));
				o.setRefreshToken(rs.getString("refresh_token"));
				Timestamp ts = rs.getTimestamp("expires_at");
				if (ts != null) o.setExpiresAt(ts.toInstant().toString());
				return o;
			}
		}
	}
}

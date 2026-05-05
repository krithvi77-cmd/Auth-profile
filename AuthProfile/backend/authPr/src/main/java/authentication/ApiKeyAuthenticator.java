package authentication;

import model.AuthProfile;
import model.Connection;
import model.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class ApiKeyAuthenticator implements Authenticator {

	public static final int AUTH_TYPE = 3;

	@Override
	public int authType() {
		return AUTH_TYPE;
	}

	@Override
	public String validate(AuthProfile profile, Connection conn) {
		if (conn.getName() == null || conn.getName().trim().isEmpty()) {
			return "Connection name is required";
		}
		if (profile.getFields() == null || profile.getFields().isEmpty()) {
			return "API Key profile has no field defined";
		}
		if (conn.getFields() == null || conn.getFields().isEmpty()) {
			return "API key value is required";
		}

		Map<String, String> supplied = AuthUtil.toMap(conn);

		Field target = resolveApiKeyField(profile, supplied);
		if (target == null) {
			return "Supplied key does not match any field on this profile";
		}

		String v = supplied.get(target.getKey());

		if (v == null) {
			v = supplied.get("api_key_value");
		}
		if (v == null || v.isEmpty()) {
			return (target.getLabel() != null ? target.getLabel() : target.getKey()) + " is required";
		}
		return null;
	}

	@Override
	public int save(java.sql.Connection jdbc, AuthProfile profile, Connection conn) throws SQLException {
		Map<String, String> supplied = AuthUtil.toMap(conn);
		Field target = resolveApiKeyField(profile, supplied);

		String value = supplied.get(target.getKey());
		if (value == null) value = supplied.get("api_key_value");

		int valueId = AuthUtil.insertValue(jdbc, 0, target.getId(), target.getKey(), value);

		int connectionId = insertConnection(jdbc, profile, conn,
				Connection.VALUE_TYPE_VALUES, valueId);

		AuthUtil.assignConnectionIdToValueRow(jdbc, valueId, connectionId);

		conn.setValueType(Connection.VALUE_TYPE_VALUES);
		conn.setValueId(valueId);
		return connectionId;
	}


	private Field resolveApiKeyField(AuthProfile profile, Map<String, String> supplied) {
		for (Field f : profile.getFields()) {
			if (supplied.containsKey(f.getKey())){
				return f;
			}
		}
		for (Field f : profile.getFields()) {
			if (f.isCustom()) {
				return f;
			}
		}
		return profile.getFields().isEmpty() ? null : profile.getFields().get(0);
	}

	private int insertConnection(java.sql.Connection jdbc, AuthProfile profile, Connection conn,
			String valueType, int valueId) throws SQLException {
		String sql = "INSERT INTO connections (profile_id, user_id, name, status, value_type, value_id) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = jdbc.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, profile.getId());
			int userId = conn.getUserId() != null ? conn.getUserId()
			           : (profile.getCreatedBy() != null ? profile.getCreatedBy() : 0);
			ps.setInt(2, userId);
			ps.setString(3, conn.getName().trim());
			ps.setString(4, conn.getStatus() != null ? conn.getStatus() : "active");
			ps.setString(5, valueType);
			ps.setInt(6, valueId);
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (!keys.next()) throw new SQLException("Insert connections failed, no id returned");
				return keys.getInt(1);
			}
		}
	}
}

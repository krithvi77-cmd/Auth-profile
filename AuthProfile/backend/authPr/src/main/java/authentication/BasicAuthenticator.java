package authentication;

import model.AuthProfile;
import model.Connection;
import model.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class BasicAuthenticator implements Authenticator {

	public static final int AUTH_TYPE = 1;

	@Override
	public int authType() {
		return AUTH_TYPE;
	}

	@Override
	public String validate(AuthProfile profile, Connection conn) {
		if (conn.getName() == null || conn.getName().trim().isEmpty()) {
			return "Connection name is required";
		}
		Map<String, String> supplied = AuthUtil.toMap(conn);

		String username = supplied.get("username");
		String password = supplied.get("password");

		if (username == null || username.trim().isEmpty())
			return "username is required";
		if (password == null || password.isEmpty())
			return "password is required";

		if (AuthUtil.findField(profile, "username") == null) {
			return "Profile missing 'username' field";
		}
		if (AuthUtil.findField(profile, "password") == null) {
			return "Profile missing 'password' field";
		}

		return null;
	}

	@Override
	public int save(java.sql.Connection jdbc, AuthProfile profile, Connection conn) throws SQLException {
		Map<String, String> supplied = AuthUtil.toMap(conn);
		Field userField = AuthUtil.findField(profile, "username");

		Map<String, String> valuesMap = new LinkedHashMap<>();
		valuesMap.put("username", supplied.get("username"));
		valuesMap.put("password", supplied.get("password"));

		int valueId = AuthUtil.insertValueAsJson(jdbc, userField.getId(), "username", valuesMap);

		int connectionId = insertConnection(jdbc, profile, conn,
				Connection.VALUE_TYPE_VALUES, valueId);

		conn.setValueType(Connection.VALUE_TYPE_VALUES);
		conn.setValueId(valueId);
		return connectionId;
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
				if (!keys.next())
					throw new SQLException("Insert connections failed, no id returned");
				return keys.getInt(1);
			}
		}
	}
}

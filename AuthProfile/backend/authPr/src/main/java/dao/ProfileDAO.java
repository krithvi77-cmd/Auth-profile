package dao;
import model.AuthProfile;
import model.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProfileDAO {

	public static final String MASK_SENTINEL = "********";

	public List<AuthProfile> getAll() throws SQLException {
		List<AuthProfile> list = new ArrayList<>();
		String sql = "SELECT id, name, auth_type, version, created_by, is_active, created_at "
				+ "FROM profiles WHERE is_active = 1";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapProfile(rs));
			}
		}
		return list;
	}

	public AuthProfile getById(int id) throws SQLException {
		String sql = "SELECT id, name, auth_type, version, created_by, is_active, created_at "
				+ "FROM profiles WHERE id = ?";
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return null;
				AuthProfile p = mapProfile(rs);
				p.setFields(getFieldsForProfile(p.getId()));
				return p;
			}
		}
	}

	private List<Field> getFieldsForProfile(int profileId) throws SQLException {
		return getFieldsForProfile(profileId, true);
	}

	private List<Field> getFieldsForProfile(int profileId, boolean maskSecrets) throws SQLException {
		List<Field> fields = new ArrayList<>();
		String sql = "SELECT id, profile_id, `key`, label, field_type, default_value, "
				+ "       is_custom, placement, position "
				+ "FROM profile_fields WHERE profile_id = ? ORDER BY position";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, profileId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					fields.add(mapField(rs, maskSecrets));
			}
		}
		return fields;
	}

	public AuthProfile getByIdUnmasked(int id) throws SQLException {
		String sql = "SELECT id, name, auth_type, version, created_by, is_active, created_at "
				+ "FROM profiles WHERE id = ?";
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				AuthProfile p = mapProfile(rs);
				p.setFields(getFieldsForProfile(p.getId(), false));
				return p;
			}
		}
	}

	public int create(AuthProfile profile) throws SQLException {
		String insertProfile = "INSERT INTO profiles (name, auth_type, version, created_by, is_active) "
				+ "VALUES (?, ?, ?, ?, 1)";

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				int profileId;
				try (PreparedStatement ps = con.prepareStatement(insertProfile, Statement.RETURN_GENERATED_KEYS)) {
					ps.setString(1, profile.getName());
					ps.setInt(2, profile.getAuthType());
					ps.setInt(3, profile.getVersion() > 0 ? profile.getVersion() : 1);
					if (profile.getCreatedBy() != null)
						ps.setInt(4, profile.getCreatedBy());
					else
						ps.setNull(4, Types.INTEGER);

					ps.executeUpdate();
					try (ResultSet keys = ps.getGeneratedKeys()) {
						if (!keys.next())
							throw new SQLException("Insert failed, no id.");
						profileId = keys.getInt(1);
					}
				}

				insertFields(con, profileId, profile.getFields());
				con.commit();
				profile.setId(profileId);
				return profileId;
			} catch (SQLException ex) {
				con.rollback();
				throw ex;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

	public boolean update(AuthProfile profile) throws SQLException {
		if (profile == null || profile.getId() <= 0) {
			throw new IllegalArgumentException("profile id is required for update");
		}

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				AuthProfile stored = getByIdUnmaskedNoTx(con, profile.getId());
				if (stored == null) {
					con.rollback();
					return false;
				}

				if (profile.getAuthType() > 0 && profile.getAuthType() != stored.getAuthType()) {
					throw new IllegalArgumentException(
							"auth_type is immutable on an existing profile (current="
									+ stored.getAuthType() + ", requested=" + profile.getAuthType()
									+ "). Create a new profile instead.");
				}

				assertFieldSetUnchanged(stored.getFields(), profile.getFields());

				int rows;
				try (PreparedStatement ps = con.prepareStatement(
						"UPDATE profiles SET name = ?, version = version + 1 WHERE id = ?")) {
					ps.setString(1, profile.getName());
					ps.setInt(2, profile.getId());
					rows = ps.executeUpdate();
				}
				if (rows == 0) {
					con.rollback();
					return false;
				}

				updateFieldsInPlace(con, stored, profile.getFields());

				con.commit();
				return true;
			} catch (SQLException | IllegalArgumentException ex) {
				con.rollback();
				throw ex;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

	private AuthProfile getByIdUnmaskedNoTx(Connection con, int id) throws SQLException {
		AuthProfile p;
		try (PreparedStatement ps = con.prepareStatement(
				"SELECT id, name, auth_type, version, created_by, is_active, created_at "
						+ "FROM profiles WHERE id = ?")) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				p = mapProfile(rs);
			}
		}
		List<Field> fields = new ArrayList<>();
		try (PreparedStatement ps = con.prepareStatement(
				"SELECT id, profile_id, `key`, label, field_type, default_value, "
						+ "       is_custom, placement, position "
						+ "FROM profile_fields WHERE profile_id = ? ORDER BY position")) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) fields.add(mapField(rs, false));
			}
		}
		p.setFields(fields);
		return p;
	}

	private void assertFieldSetUnchanged(List<Field> stored, List<Field> incoming) {
		if (incoming == null || incoming.isEmpty()) return;

		Map<String, Field> storedByKey = new HashMap<>();
		for (Field f : stored) {
			if (f.getKey() != null) storedByKey.put(f.getKey().toLowerCase(), f);
		}
		Map<String, Field> incomingByKey = new LinkedHashMap<>();
		for (Field f : incoming) {
			if (f.getKey() == null || f.getKey().isEmpty()) {
				throw new IllegalArgumentException("field key is required");
			}
			String k = f.getKey().toLowerCase();
			if (incomingByKey.containsKey(k)) {
				throw new IllegalArgumentException("duplicate field key in request: " + f.getKey());
			}
			incomingByKey.put(k, f);
		}

		for (String k : incomingByKey.keySet()) {
			if (!storedByKey.containsKey(k)) {
				throw new IllegalArgumentException(
						"cannot add new field '" + k + "' to an existing profile. "
								+ "The field set is fixed at creation time. Create a new profile instead.");
			}
		}

		for (String k : storedByKey.keySet()) {
			if (!incomingByKey.containsKey(k)) {
				throw new IllegalArgumentException(
						"cannot remove field '" + k + "' from an existing profile. "
								+ "The field set is fixed at creation time.");
			}
		}
		for (Map.Entry<String, Field> e : incomingByKey.entrySet()) {
			Field s = storedByKey.get(e.getKey());
			Field n = e.getValue();
			if (n.getFieldType() != null && !n.getFieldType().equalsIgnoreCase(s.getFieldType())) {
				throw new IllegalArgumentException(
						"cannot change field_type of '" + e.getKey() + "' (current="
								+ s.getFieldType() + ", requested=" + n.getFieldType() + ")");
			}
		}
	}

	private void updateFieldsInPlace(Connection con, AuthProfile stored, List<Field> incoming)
			throws SQLException {
		if (incoming == null || incoming.isEmpty()) return;

		Map<String, Field> storedByKey = new HashMap<>();
		for (Field f : stored.getFields()) {
			if (f.getKey() != null) storedByKey.put(f.getKey().toLowerCase(), f);
		}

		String sql = "UPDATE profile_fields SET label = ?, default_value = ?, "
				+ "       placement = ?, position = ? "
				+ "WHERE id = ?";

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			int pos = 1;
			for (Field in : incoming) {
				Field stRow = storedByKey.get(in.getKey().toLowerCase());

				if (in.isCustom() && in.getLabel() != null) {
					ps.setString(1, in.getLabel());
				} else {
					ps.setNull(1, Types.VARCHAR);
				}

				String resolved = resolveDefaultValueForUpdate(stRow, in);
				if (resolved != null) ps.setString(2, resolved);
				else                  ps.setNull(2, Types.VARCHAR);

				if (in.getPlacement() != null && !in.getPlacement().isEmpty()) {
					ps.setString(3, in.getPlacement());
				} else {
					ps.setNull(3, Types.VARCHAR);
				}

				ps.setInt(4, in.getPosition() > 0 ? in.getPosition() : pos);
				ps.setInt(5, stRow.getId());
				ps.addBatch();
				pos++;
			}
			ps.executeBatch();
		}
	}

	private String resolveDefaultValueForUpdate(Field stored, Field incoming) {
		String incomingVal = incoming.getDefaultValue();
		boolean isSecret = "password".equalsIgnoreCase(stored.getFieldType());
		if (isSecret) {
			boolean keepStored = incomingVal == null
					|| incomingVal.isEmpty()
					|| MASK_SENTINEL.equals(incomingVal);
			return keepStored ? stored.getDefaultValue() : incomingVal;
		}
		return incomingVal;
	}

	public boolean delete(int id) throws SQLException {
		String sql = "UPDATE profiles SET is_active = 0 WHERE id = ?";
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean hardDelete(int id) throws SQLException {
		String sql = "DELETE FROM profiles WHERE id = ?";
		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}

	private void insertFields(Connection con, int profileId, List<Field> fields) throws SQLException {
		if (fields == null || fields.isEmpty())
			return;

		String sql = "INSERT INTO profile_fields "
				+ "(profile_id, `key`, label, field_type, default_value, is_custom, placement, position) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			int pos = 1;
			for (Field f : fields) {
				ps.setInt(1, profileId);
				ps.setString(2, f.getKey());

				if (f.isCustom() && f.getLabel() != null) {
					ps.setString(3, f.getLabel());
				} else {
					ps.setNull(3, Types.VARCHAR);
				}

				ps.setString(4, f.getFieldType() != null ? f.getFieldType() : "text");
				ps.setString(5, f.getDefaultValue());
				ps.setBoolean(6, f.isCustom());

				if (f.getPlacement() != null && !f.getPlacement().isEmpty()) {
					ps.setString(7, f.getPlacement());
				} else {
					ps.setNull(7, Types.VARCHAR);
				}

				ps.setInt(8, f.getPosition() > 0 ? f.getPosition() : pos);
				ps.addBatch();
				pos++;
			}
			ps.executeBatch();
		}
	}

	private AuthProfile mapProfile(ResultSet rs) throws SQLException {
		AuthProfile p = new AuthProfile();
		p.setId(rs.getInt("id"));
		p.setName(rs.getString("name"));
		p.setAuthType(rs.getInt("auth_type"));
		p.setVersion(rs.getInt("version"));
		int cb = rs.getInt("created_by");
		p.setCreatedBy(rs.wasNull() ? null : cb);
		p.setActive(rs.getBoolean("is_active"));
		Timestamp ts = rs.getTimestamp("created_at");
		if (ts != null)
			p.setCreatedAt(ts.toString());
		return p;
	}

	private Field mapField(ResultSet rs, boolean maskSecrets) throws SQLException {
		Field f = new Field();
		f.setId(rs.getInt("id"));
		f.setProfileId(rs.getInt("profile_id"));
		f.setKey(rs.getString("key"));
		f.setLabel(rs.getString("label"));
		String type = rs.getString("field_type");
		f.setFieldType(type);

		String def = rs.getString("default_value");
		if (maskSecrets
				&& "password".equalsIgnoreCase(type)
				&& def != null
				&& !def.isEmpty()) {
			def = MASK_SENTINEL;
		}
		f.setDefaultValue(def);

		f.setCustom(rs.getBoolean("is_custom"));
		f.setPlacement(rs.getString("placement"));
		f.setPosition(rs.getInt("position"));
		return f;
	}
}

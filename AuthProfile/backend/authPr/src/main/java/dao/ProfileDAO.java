package dao;
import model.AuthProfile;
import model.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO {

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
		List<Field> fields = new ArrayList<>();
		String sql = "SELECT id, profile_id, `key`, label, field_type, default_value, "
				+ "       is_custom, placement, position "
				+ "FROM profile_fields WHERE profile_id = ? ORDER BY position";

		try (Connection con = DBUtil.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, profileId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					fields.add(mapField(rs));
			}
		}
		return fields;
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

	private boolean deleteField(Connection con,AuthProfile profile) throws SQLException {
		try (PreparedStatement del = con.prepareStatement(
				"DELETE FROM profile_fields WHERE profile_id = ?")) {
			del.setInt(1, profile.getId());
			del.executeUpdate();
			 return true;
		}
			catch (SQLException ex){
			con.rollback();
			return false;
	  }
	}

	public boolean update(AuthProfile profile) throws SQLException {
		String updateSql = "UPDATE profiles SET name = ?, auth_type = ?, version = version + 1 "
				+ "WHERE id = ?";

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false);
			try {
				int rows;
				try (PreparedStatement ps = con.prepareStatement(updateSql)) {
					ps.setString(1, profile.getName());
					ps.setInt(2, profile.getAuthType());
					ps.setInt(3, profile.getId());
					rows = ps.executeUpdate();
				}
				if (rows == 0) {
					con.rollback();
					return false;
				}

			    if(!deleteField(con,profile)){
					return false;
				}
				insertFields(con, profile.getId(), profile.getFields());
				con.commit();
				return true;
			} catch (SQLException ex) {
				con.rollback();
				throw ex;
			} finally {
				con.setAutoCommit(true);
			}
		}
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

	private Field mapField(ResultSet rs) throws SQLException {
		Field f = new Field();
		f.setId(rs.getInt("id"));
		f.setProfileId(rs.getInt("profile_id"));
		f.setKey(rs.getString("key"));
		f.setLabel(rs.getString("label"));  
		f.setFieldType(rs.getString("field_type"));
		f.setDefaultValue(rs.getString("default_value"));
		f.setCustom(rs.getBoolean("is_custom"));
		f.setPlacement(rs.getString("placement")); 
		f.setPosition(rs.getInt("position"));
		return f;
	}
}

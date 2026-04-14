package dao;

import model.*;
import java.sql.*;
import java.util.*;

public class AuthProfileDAO {

	public void save(AuthProfile profile) throws Exception {

		Connection con = DBUtil.getConnection();

		String sql = "INSERT INTO auth_profile VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = con.prepareStatement(sql);

		ps.setInt(1, profile.getId());
		ps.setString(2, profile.getName());
		ps.setInt(3, profile.getAuthtype().getId());
		ps.setString(4, profile.getCreatedBy());
		ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

		ps.executeUpdate();

		String valSql = "INSERT INTO auth_profile_values(profile_id, field_id, value) VALUES (?, ?, ?)";
		PreparedStatement valPs = con.prepareStatement(valSql);

		for (Map.Entry<Integer, String> entry : profile.getValues().entrySet()) {

			valPs.setInt(1, profile.getId());
			valPs.setInt(2, entry.getKey());
			valPs.setString(3, entry.getValue());

			valPs.addBatch();
		}

		valPs.executeBatch();
	}

	public AuthProfile getById(int id) throws Exception {

		Connection con = DBUtil.getConnection();

		String sql = "SELECT * FROM auth_profile WHERE id = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, id);

		ResultSet rs = ps.executeQuery();

		if (!rs.next())
			return null;

		AuthProfile profile = new AuthProfile(rs.getString("name"), null, rs.getString("created_by"),
				rs.getTimestamp("updated_on"), new HashMap<>());

		profile.setId(id);

		int authTypeId = rs.getInt("auth_type_id");

		AuthTypeDAO typeDAO = new AuthTypeDAO();
		profile.setAuthtype(typeDAO.getById(authTypeId));

		String valSql = "SELECT field_id, value FROM auth_profile_values WHERE profile_id = ?";
		PreparedStatement valPs = con.prepareStatement(valSql);
		valPs.setInt(1, id);

		ResultSet valRs = valPs.executeQuery();

		Map<Integer, String> values = new HashMap<>();

		while (valRs.next()) {
			values.put(valRs.getInt("field_id"), valRs.getString("value"));
		}

		profile.setValues(values);

		return profile;
	}
}
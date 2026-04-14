package dao;

import model.AuthType;
import java.sql.*;
import java.util.*;

public class AuthTypeDAO {

	public void save(AuthType type) throws Exception {
		Connection con = DBUtil.getConnection();

		String sql = "INSERT INTO auth_type(name) VALUES (?)";
		PreparedStatement ps = con.prepareStatement(sql);

		ps.setString(1, type.getName());
		ps.executeUpdate();
	}

	public List<AuthType> getAll() throws Exception {
		List<AuthType> list = new ArrayList<>();

		Connection con = DBUtil.getConnection();
		String sql = "SELECT * FROM auth_type";

		ResultSet rs = con.prepareStatement(sql).executeQuery();

		while (rs.next()) {
			AuthType t = new AuthType();
			t.setId(rs.getInt("id"));
			t.setName(rs.getString("name"));
			list.add(t);
		}

		return list;
	}

	public AuthType getById(int id) throws Exception {
		Connection con = DBUtil.getConnection();

		String sql = "SELECT * FROM auth_type WHERE id = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, id);

		ResultSet rs = ps.executeQuery();

		if (!rs.next())
			return null;

		AuthType t = new AuthType();
		t.setId(rs.getInt("id"));
		t.setName(rs.getString("name"));

		return t;
	}
}

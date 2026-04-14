package dao;

import model.Option;
import java.sql.*;
import java.util.*;

public class OptionDAO {

	public void save(int fieldId, Option option) throws Exception {

		Connection con = DBUtil.getConnection();

		String sql = "INSERT INTO field_options(field_id, label, value) VALUES (?, ?, ?)";
		PreparedStatement ps = con.prepareStatement(sql);

		ps.setInt(1, fieldId);
		ps.setString(2, option.getLabel());
		ps.setString(3, option.getValue());

		ps.executeUpdate();
	}

	public List<Option> getByField(int fieldId) throws Exception {

		List<Option> list = new ArrayList<>();
		Connection con = DBUtil.getConnection();

		String sql = "SELECT * FROM field_options WHERE field_id = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, fieldId);

		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			Option o = new Option();
			o.setId(rs.getInt("id"));
			o.setLabel(rs.getString("label"));
			o.setValue(rs.getString("value"));
			list.add(o);
		}

		return list;
	}
}
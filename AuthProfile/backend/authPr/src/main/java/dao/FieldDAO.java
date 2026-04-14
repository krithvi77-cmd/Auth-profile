package dao;

import model.Field;
import java.sql.*;
import java.util.*;

public class FieldDAO {

	public int save(Field field, int authTypeId) throws Exception {

		Connection con = DBUtil.getConnection();

		String sql = "INSERT INTO fields(name, label, type) VALUES (?, ?, ?)";
		PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

		ps.setString(1, field.getName());
		ps.setString(2, field.getLabel());
		ps.setString(3, field.getType());

		ps.executeUpdate();

		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		int fieldId = rs.getInt(1);

		String mapSql = "INSERT INTO auth_type_field(auth_type_id, field_id, required) VALUES (?, ?, ?)";
		PreparedStatement mapPs = con.prepareStatement(mapSql);

		mapPs.setInt(1, authTypeId);
		mapPs.setInt(2, fieldId);
		mapPs.setBoolean(3, field.isRequired());

		mapPs.executeUpdate();

		return fieldId;
	}

	public List<Field> getByAuthType(int authTypeId) throws Exception {

		List<Field> fields = new ArrayList<>();
		Connection con = DBUtil.getConnection();

		String sql = """
				    SELECT f.id, f.name, f.label, f.type, atf.required
				    FROM auth_type_field atf
				    JOIN fields f ON atf.field_id = f.id
				    WHERE atf.auth_type_id = ?
				""";

		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, authTypeId);

		ResultSet rs = ps.executeQuery();

		OptionDAO optionDAO = new OptionDAO();

		while (rs.next()) {
			Field f = new Field();

			f.setId(rs.getInt("id"));
			f.setName(rs.getString("name"));
			f.setLabel(rs.getString("label"));
			f.setType(rs.getString("type"));
			f.setRequired(rs.getBoolean("required"));

			if ("select".equalsIgnoreCase(f.getType())) {
				f.setOptions(optionDAO.getByField(f.getId()));
			}

			fields.add(f);
		}

		return fields;
	}
}
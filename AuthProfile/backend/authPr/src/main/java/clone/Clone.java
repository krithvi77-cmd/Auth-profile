package clone;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dao.DBUtil;
import model.AuthProfile;
import model.Connection;

public class Clone {

    public int cloneConnection(Connection source, AuthProfile profile, String newName) throws SQLException {
        if (source == null) {
            throw new IllegalArgumentException("Source connection is required");
        }
        if (profile == null) {
            throw new IllegalArgumentException("Auth profile is required");
        }
        if (source.getValueType() == null || source.getValueId() == null) {
            throw new IllegalStateException("Source connection has no value reference to clone");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Clone name is required");
        }

        String sql = "INSERT INTO connections (profile_id, user_id, name, status, value_type, value_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection jdbc = DBUtil.getConnection();
                PreparedStatement ps = jdbc.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int userId = source.getUserId() != null ? source.getUserId()
                    : (profile.getCreatedBy() != null ? profile.getCreatedBy() : 0);

            ps.setInt(1, profile.getId());
            ps.setInt(2, userId);
            ps.setString(3, newName.trim());
            ps.setString(4, source.getStatus() != null ? source.getStatus() : "active");
            ps.setString(5, source.getValueType());
            ps.setInt(6, source.getValueId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Insert into connections failed, no id returned");
                }
                return keys.getInt(1);
            }
            
        }
    }
}

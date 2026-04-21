package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String URL      = "jdbc:mysql://localhost:3306/auth_profiles";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Krithvi@1234";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found on classpath", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new IllegalStateException(
                "Failed to connect to MySQL. URL=" + URL + ", USERNAME=" + USERNAME + ", ERROR=" + e.getMessage(),
                e
            );
        }
    }
}

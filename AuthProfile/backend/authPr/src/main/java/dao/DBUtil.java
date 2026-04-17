package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	

   public static Connection getConnection() {
	   String url = "jdbc:mysql://localhost:3306/auth_profiles";
	   String username = "root";
	   String password = "Krithvi-123";
	   Connection con = null;
		try {
			con = DriverManager.getConnection(url,username,password);
		} catch (SQLException e) {
  			e.printStackTrace();
  		  System.err.println("[ERROR]: Can't connect to the DB , connection is not made");
		}
	   return con;
   }
   
}

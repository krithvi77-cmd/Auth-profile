package authentication;

import model.AuthProfile;
import model.Connection;

import java.sql.SQLException;



public interface Authenticator {

	int authType();


	String validate(AuthProfile profile, Connection conn);


	int save(java.sql.Connection jdbc, AuthProfile profile, Connection conn) throws SQLException;
}

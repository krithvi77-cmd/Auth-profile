package test;

import dao.ConnectionDAO;
import dao.DBUtil;
import model.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionRefactorTest {

	private static int passed = 0;
	private static int failed = 0;

	public static void main(String[] args) {
		System.out.println("=== Running Connection Schema Tests ===");
		try {
			testNotNullValueType();
			testNotNullValueId();
			testInvalidEnumValueType();
			testUniqueValueRef();
			testReadValuesConnection();
			testReadOauthConnection();
		} catch (Exception ex) {
			System.err.println("Test suite aborted: " + ex.getMessage());
			ex.printStackTrace();
		}

		System.out.println("\n=== Results ===");
		System.out.println("Passed: " + passed);
		System.out.println("Failed: " + failed);
		if (failed > 0) {
			System.exit(1);
		}
	}

	static void testNotNullValueType() {
		String testName = "NOT NULL: value_type cannot be null";
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(
						"INSERT INTO connections (profile_id, user_id, name, status, value_type, value_id) "
								+ "VALUES (?, 0, 'test-null-vtype', 'active', NULL, 1)")) {
			ps.setInt(1, anyExistingProfileId(jdbc));
			ps.executeUpdate();
			fail(testName, "Expected SQLException, but insert succeeded");
		} catch (SQLException expected) {
			pass(testName + " — rejected as expected: " + expected.getMessage());
		}
	}

	static void testNotNullValueId() {
		String testName = "NOT NULL: value_id cannot be null";
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(
						"INSERT INTO connections (profile_id, user_id, name, status, value_type, value_id) "
								+ "VALUES (?, 0, 'test-null-vid', 'active', 'VALUES', NULL)")) {
			ps.setInt(1, anyExistingProfileId(jdbc));
			ps.executeUpdate();
			fail(testName, "Expected SQLException, but insert succeeded");
		} catch (SQLException expected) {
			pass(testName + " — rejected as expected: " + expected.getMessage());
		}
	}

	static void testInvalidEnumValueType() {
		String testName = "ENUM: value_type rejects unknown values";
		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			PreparedStatement ps = jdbc.prepareStatement(
					"INSERT INTO connections (profile_id, user_id, name, status, value_type, value_id) "
							+ "VALUES (?, 0, 'test-bad-enum', 'active', 'INVALID', 1)");
			ps.setInt(1, anyExistingProfileId(jdbc));
			try {
				ps.executeUpdate();
				fail(testName, "Expected SQLException for invalid enum value");
			} catch (SQLException expected) {
				pass(testName + " — rejected as expected: " + expected.getMessage());
			}
		} catch (SQLException ex) {
			fail(testName, ex.getMessage());
		}
	}

	static void testUniqueValueRef() {
		String testName = "UNIQUE: (value_type, value_id) prevents duplicates";
		try (java.sql.Connection jdbc = DBUtil.getConnection()) {
			Integer profileId = null;
			String existingType = null;
			Integer existingId = null;
			try (PreparedStatement ps = jdbc.prepareStatement(
					"SELECT profile_id, value_type, value_id FROM connections LIMIT 1");
					ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					profileId = rs.getInt(1);
					existingType = rs.getString(2);
					existingId = rs.getInt(3);
				}
			}
			if (profileId == null) {
				skip(testName, "No existing connections to test against");
				return;
			}

			try (PreparedStatement ps = jdbc.prepareStatement(
					"INSERT INTO connections (profile_id, user_id, name, status, value_type, value_id) "
							+ "VALUES (?, 0, 'test-dup', 'active', ?, ?)")) {
				ps.setInt(1, profileId);
				ps.setString(2, existingType);
				ps.setInt(3, existingId);
				ps.executeUpdate();
				fail(testName, "Expected duplicate-key error, but insert succeeded");
			} catch (SQLException expected) {
				pass(testName + " — rejected as expected: " + expected.getMessage());
			}
		} catch (SQLException ex) {
			fail(testName, ex.getMessage());
		}
	}

	static void testReadValuesConnection() {
		String testName = "DAO: reads VALUES-type connection";
		try {
			ConnectionDAO dao = new ConnectionDAO();
			Integer cid = findConnectionByType("VALUES");
			if (cid == null) {
				skip(testName, "No VALUES-type connection in DB");
				return;
			}
			Connection conn = dao.getById(cid);
			if (conn == null) {
				fail(testName, "DAO returned null for id=" + cid);
				return;
			}
			assertEqual(testName + " - valueType", "VALUES", conn.getValueType());
			assertNotNull(testName + " - valueId", conn.getValueId());
			assertEqual(testName + " - connectionType", "values", conn.getConnectionType());
			assertNotNull(testName + " - fields loaded", conn.getFields());
			pass(testName);
		} catch (SQLException ex) {
			fail(testName, ex.getMessage());
		}
	}

	static void testReadOauthConnection() {
		String testName = "DAO: reads OAUTH-type connection";
		try {
			ConnectionDAO dao = new ConnectionDAO();
			Integer cid = findConnectionByType("OAUTH");
			if (cid == null) {
				skip(testName, "No OAUTH-type connection in DB");
				return;
			}
			Connection conn = dao.getById(cid);
			if (conn == null) {
				fail(testName, "DAO returned null for id=" + cid);
				return;
			}
			assertEqual(testName + " - valueType", "OAUTH", conn.getValueType());
			assertNotNull(testName + " - valueId", conn.getValueId());
			assertEqual(testName + " - connectionType", "oauth", conn.getConnectionType());
			pass(testName);
		} catch (SQLException ex) {
			fail(testName, ex.getMessage());
		}
	}

	private static Integer findConnectionByType(String valueType) throws SQLException {
		try (java.sql.Connection jdbc = DBUtil.getConnection();
				PreparedStatement ps = jdbc.prepareStatement(
						"SELECT id FROM connections WHERE value_type = ? LIMIT 1")) {
			ps.setString(1, valueType);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : null;
			}
		}
	}

	private static int anyExistingProfileId(java.sql.Connection jdbc) throws SQLException {
		try (Statement st = jdbc.createStatement();
				ResultSet rs = st.executeQuery("SELECT id FROM profiles LIMIT 1")) {
			if (rs.next()) return rs.getInt(1);
			throw new SQLException("No profiles available for test");
		}
	}

	private static void pass(String testName) {
		passed++;
		System.out.println("PASS: " + testName);
	}

	private static void fail(String testName, String msg) {
		failed++;
		System.out.println("FAIL: " + testName + " — " + msg);
	}

	private static void skip(String testName, String reason) {
		System.out.println("SKIP: " + testName + " (" + reason + ")");
	}

	private static void assertEqual(String label, Object expected, Object actual) {
		if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
			pass(label + " == " + expected);
		} else {
			fail(label, "expected=" + expected + ", actual=" + actual);
		}
	}

	private static void assertNotNull(String label, Object actual) {
		if (actual != null) {
			pass(label + " is non-null");
		} else {
			fail(label, "expected non-null, got null");
		}
	}
}

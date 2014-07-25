package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class TestUtilities {
	private static Connection conn;
	private static final String URL;
	private static final String USERNAME;
	private static final String PASSWORD;
	
	static {
		URL = "jdbc:mysql://localhost:3306/recruitment_test_database";
		USERNAME = "root";
		PASSWORD = "letmein";
	}
	static void resetTestDatabase() {
		// resets the database state so that no table data exists
		try {
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			Statement statement = conn.createStatement();
			statement.executeUpdate("DELETE FROM user");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

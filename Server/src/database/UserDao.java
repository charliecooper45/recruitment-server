package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import security.BCrypt;

/**
 * Processes requests for the user table in the recruitment database.
 * @author Charlie
 */
public class UserDao {
	//TODO NEXT: combine these methods
	public boolean checkValidUser(String userId) {
		boolean accountStatus;
		
		// check if the user exists
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT account_status " +
					"FROM user WHERE user_id = ?");
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				accountStatus = rs.getBoolean("account_status");
				
				// check if the user`s account is open
				if(accountStatus == false) {
					return false;
				}
			} else {
				// the user does not exist, return false
				return false;
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public String checkPassword(String userId, String password) {
		//TODO NEXT: implement this
		String databasePassword = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT password, account_type " +
					"FROM user WHERE user_id = ?");
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				databasePassword = rs.getString("password");
				if(BCrypt.checkpw(password, databasePassword)) {
					String accountType = rs.getString("account_type");
					return accountType;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
	}
}

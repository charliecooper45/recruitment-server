package database;

import interfaces.UserType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import security.BCrypt;
import database.beans.User;

/**
 * Processes requests for the user table in the recruitment database.
 * @author Charlie
 */
public class UserDao {
	public boolean checkValidUser(String userId) {
		boolean accountStatus;

		// check if the user exists
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT account_status " + "FROM user WHERE user_id = ?");
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				accountStatus = rs.getBoolean("account_status");

				// check if the user`s account is open
				if (accountStatus == false) {
					return false;
				}
			} else {
				// the user does not exist, return false
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public String checkPassword(String userId, String password) {
		//TODO NEXT: implement this
		String databasePassword = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT password, account_type " + "FROM user WHERE user_id = ?");
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				databasePassword = rs.getString("password");
				if (BCrypt.checkpw(password, databasePassword)) {
					String accountType = rs.getString("account_type");
					return accountType;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
	}

	public List<User> getUsers(UserType userType, boolean status) {
		PreparedStatement statement = null;
		List<User> users = new ArrayList<User>();
		String userTypeString;

		if (userType == null) {
			// get both types of user
			userTypeString = "%";
		} else {
			userTypeString = userType.toString();
		}

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			if (status) {
				// run this if only looking for active users
				statement = conn.prepareStatement("SELECT user_id, first_name, surname, email_address, phone_number, account_type, account_status FROM user WHERE account_type LIKE ? AND account_status = ?");
				statement.setString(1, userTypeString);
				statement.setBoolean(2, status);
			} else {
				// run this if looking for active and closed users
				statement = conn.prepareStatement("SELECT user_id, first_name, surname, email_address, phone_number, account_type, account_status FROM user WHERE account_type LIKE ?");
				statement.setString(1, userTypeString);
			}

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				String userId = rs.getString("user_id");
				String firstName = rs.getString("first_name");
				String surname = rs.getString("surname");
				String emailAddress = rs.getString("email_address");
				String phoneNumber = rs.getString("phone_number");
				boolean accountStatus = rs.getBoolean("account_status");
				UserType accountType = UserType.valueOf(rs.getString("account_type").toUpperCase());
				User user = new User(userId, null, firstName, surname, emailAddress, phoneNumber, accountStatus, accountType);
				users.add(user);
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}

		return users;
	}

	public UserType getUserType(String userId) {
		PreparedStatement statement = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT account_type FROM user WHERE user_id = ?");
			statement.setString(1, userId);

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				String userType = rs.getString("account_type");
				return UserType.valueOf(userType.toUpperCase());
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public boolean addUser(User user) {
		PreparedStatement statement = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO user (user_id, password, first_name, surname, email_address, phone_number, account_status, account_type) VALUES " +
					"(?, ?, ?, ?, ?,?, ?, ?)");
			statement.setString(1, user.getUserId());
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			statement.setString(2, hashedPassword);
			statement.setString(3, user.getFirstName());
			statement.setString(4, user.getSurname());
			statement.setString(5, user.getEmailAddress());
			statement.setString(6, user.getPhoneNumber());
			statement.setBoolean(7, user.getAccountStatus());
			statement.setString(8, String.valueOf(user.getAccountType()).toLowerCase());
			int result = statement.executeUpdate();
			
			if(result != 0) {
				return true;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		return false;
	}
}

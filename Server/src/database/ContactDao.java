package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.beans.Contact;
import database.beans.Organisation;

/**
 * Processes requests for the contact table in the recruitment database.
 * @author Charlie
 */
public class ContactDao {

	public List<Contact> getOrganisationsContacts(Organisation organisation) {
		List<Contact> contacts = new ArrayList<>();
		int id = -1;
		//TODO NEXT B: Not all of these variables are used below
		String firstName, surname, jobTitle, phoneNumber, emailAddress, address, notes, organisationId, userId;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			PreparedStatement statement = conn.prepareStatement("SELECT contact_id, first_name, surname, job_title, phone_number, email_address, address, " +
					"notes, organisation_organisation_id, user_user_id FROM contact WHERE organisation_organisation_id = ?");
			statement.setInt(1, organisation.getId());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				id = rs.getInt("contact_id");
				firstName = rs.getString("first_name");
				surname = rs.getString("surname");
				phoneNumber = rs.getString("phone_number");
				contacts.add(new Contact(id, firstName, surname, null, phoneNumber, null, null, null, -1, null));
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return contacts;
	}

	public boolean addContact(Contact contact) {
		PreparedStatement statement = null;

		// add the contact
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO contact (first_name, surname, job_title, phone_number, email_address, address, notes, organisation_organisation_id" +
					", user_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, contact.getFirstName());
			statement.setString(2, contact.getSurname());
			statement.setString(3, contact.getJobTitle());
			statement.setString(4, contact.getPhoneNumber());
			statement.setString(5, contact.getEmailAddress());
			statement.setString(6, contact.getAddress());
			statement.setString(7, contact.getNotes());
			statement.setInt(8, contact.getOrganisationId());
			statement.setString(9, contact.getUserId());
			
			int value = statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean removeContact(Contact contact) {
		PreparedStatement statement = null;
		int returned = 0;
		
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM contact WHERE contact_id = ?");
			statement.setInt(1, contact.getId());
			returned = statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		if(returned != 0) {
			return true;
		} else {
			return false;
		}
	}
}

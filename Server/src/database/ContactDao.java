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
				contacts.add(new Contact(id, firstName, surname, null, null, null, null, null, -1, null));
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return contacts;
	}
}

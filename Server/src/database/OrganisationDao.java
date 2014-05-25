package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.beans.Organisation;

/**
 * Processes requests for the organisation table in the recruitment database.
 * @author Charlie
 */
public class OrganisationDao {
	public List<Organisation> getOrganisations() {	
		List<Organisation> organisations = new ArrayList<>();
		int id = -1;
		
		//TODO NEXT B: Not all of these variables are used below
		String name, phoneNumber, emailAddress, website, address, termsOfBusiness, notes, linkedInProfile, userId;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT organisation_id, organisation_name, phone_number, email_address, website, " +
					"address, terms_of_business, notes, user_user_id FROM organisation");
			
			while (rs.next()) {
				id = rs.getInt("organisation_id");
				name = rs.getString("organisation_name");
				organisations.add(new Organisation(id, name, null, null, null, null, null, null, -1));
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return organisations;
	}
}

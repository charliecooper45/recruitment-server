package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
		int noOpenVacancies = 0;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT organisation_id, organisation_name, phone_number, email_address, website, " +
					"address, terms_of_business, notes, user_user_id FROM organisation");
			
			while (rs.next()) {
				id = rs.getInt("organisation_id");
				name = rs.getString("organisation_name");
				phoneNumber = rs.getString("phone_number");
				address = rs.getString("address");
				userId = rs.getString("user_user_id");
				
				PreparedStatement noVacanciesStatement = conn.prepareStatement("SELECT COUNT(*) AS count FROM vacancy WHERE organisation_organisation_id = ?");
				noVacanciesStatement.setInt(1, id);
				ResultSet preparedRs = noVacanciesStatement.executeQuery();
				
				if(preparedRs.next()) {
					noOpenVacancies = preparedRs.getInt("count");
				}
				
				organisations.add(new Organisation(id, name, phoneNumber, null, null, address, null, null, userId, noOpenVacancies));
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return organisations;
	}

	public Organisation getOrganisation(int organisationId) {
		PreparedStatement statement;
		Organisation organisation = null;
		String name = null, phoneNumber = null, emailAddress = null, website = null, address = null, termsOfBusiness = null, notes = null, linkedInProfile = null, userId = null;
		int id = -1;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT organisation_id, organisation_name, phone_number, email_address, website, " +
					"address, terms_of_business, notes, user_user_id FROM organisation WHERE organisation_id = ?");
			statement.setInt(1, organisationId);
			
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				id = rs.getInt("organisation_id");
				name = rs.getString("organisation_name");
				phoneNumber = rs.getString("phone_number");
				emailAddress = rs.getString("email_address");
				website = rs.getString("website");
				address = rs.getString("address");
				termsOfBusiness = rs.getString("terms_of_business");
				notes = rs.getString("notes");
				userId = rs.getString("user_user_id");
			}
			organisation = new Organisation(organisationId, name, phoneNumber, emailAddress, website, address, termsOfBusiness, notes, userId, 0);
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return organisation;
	}
}

package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.ServerMain;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import database.beans.Organisation;
import database.beans.Report;

/**
 * Processes requests for the organisation table in the recruitment database.
 * @author Charlie
 */
public class OrganisationDao {
	public List<Organisation> getOrganisations() {
		List<Organisation> organisations = new ArrayList<>();
		int id = -1;

		//TODO NEXT B: Not all of these variables are used below
		String name, phoneNumber, address, userId;
		int noOpenVacancies = 0;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT organisation_id, organisation_name, phone_number, email_address, website, " + "address, terms_of_business, notes, user_user_id FROM organisation");

			while (rs.next()) {
				id = rs.getInt("organisation_id");
				name = rs.getString("organisation_name");
				phoneNumber = rs.getString("phone_number");
				address = rs.getString("address");
				userId = rs.getString("user_user_id");

				PreparedStatement noVacanciesStatement = conn.prepareStatement("SELECT COUNT(*) AS count FROM vacancy WHERE organisation_organisation_id = ?");
				noVacanciesStatement.setInt(1, id);
				ResultSet preparedRs = noVacanciesStatement.executeQuery();

				if (preparedRs.next()) {
					noOpenVacancies = preparedRs.getInt("count");
				}

				organisations.add(new Organisation(id, name, phoneNumber, null, null, address, null, null, userId, noOpenVacancies));
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return organisations;
	}

	public Organisation getOrganisation(int organisationId) {
		PreparedStatement statement;
		Organisation organisation = null;
		String name = null, phoneNumber = null, emailAddress = null, website = null, address = null, termsOfBusiness = null, notes = null, userId = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT organisation_id, organisation_name, phone_number, email_address, website, " + "address, terms_of_business, notes, user_user_id FROM organisation WHERE organisation_id = ?");
			statement.setInt(1, organisationId);

			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
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
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return organisation;
	}

	public RemoteInputStream getOrganisationTob(String fileName) {
		String fileLocation = ServerMain.ORGANISATION_TOB_FOLDER + "/" + fileName;
		Path path = Paths.get(fileLocation);

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(path.toString());
		} catch (FileNotFoundException e) {
			// TODO handle this exception
			e.printStackTrace();
			return null;
		}
		RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(inputStream);
		return remoteFileData;
	}

	public boolean addOrganisationTob(Organisation organisation, RemoteInputStream tobData, String oldFileName) {
		PreparedStatement statement = null;
		InputStream fileData;

		// add the profile to the folder
		try {
			//delete the old file if it exists
			Path path = Paths.get(ServerMain.ORGANISATION_TOB_FOLDER + "/" + oldFileName);
			if (Files.exists(path))
				Files.delete(path);

			// add the new file
			fileData = RemoteInputStreamClient.wrap(tobData);
			path = ServerMain.getCorrectFilePath(ServerMain.ORGANISATION_TOB_FOLDER, organisation.getTermsOfBusiness());
			organisation.setTermsOfBusiness(path.getFileName().toString());
			ServerMain.storeFile(fileData, path);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE organisation SET terms_of_business = ? WHERE organisation_id = ?");
			statement.setString(1, organisation.getTermsOfBusiness());
			statement.setInt(2, organisation.getOrganisationId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean removeOrganisationTob(Organisation organisation) {
		PreparedStatement statement = null;

		try {
			//delete the file
			Path path = Paths.get(ServerMain.ORGANISATION_TOB_FOLDER + "/" + organisation.getTermsOfBusiness());
			Files.delete(path);
		} catch (IOException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE organisation SET terms_of_business = null WHERE organisation_id = ?");
			statement.setInt(1, organisation.getOrganisationId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean addOrganisation(Organisation organisation, RemoteInputStream tobData) {
		PreparedStatement statement = null;
		InputStream fileData;
		Path path = null;

		if (tobData != null) {
			try {
				// add the new file if it has been specified
				fileData = RemoteInputStreamClient.wrap(tobData);
				path = ServerMain.getCorrectFilePath(ServerMain.ORGANISATION_TOB_FOLDER, organisation.getTermsOfBusiness());
				ServerMain.storeFile(fileData, path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		// add the organisation
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO organisation (organisation_name, phone_number, email_address, website, address, terms_of_business, " + "notes, user_user_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, organisation.getOrganisationName());
			statement.setString(2, organisation.getPhoneNumber());
			statement.setString(3, organisation.getEmailAddress());
			statement.setString(4, organisation.getWebsite());
			statement.setString(5, organisation.getAddress());
			statement.setString(6, organisation.getTermsOfBusiness());
			statement.setString(7, organisation.getNotes());
			statement.setString(8, organisation.getUserId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean removeOrganisation(Organisation organisation) {
		PreparedStatement statement = null;
		int returned = 0;

		// remove the TOB if they are present
		if (organisation.getTermsOfBusiness() != null) {
			try {
				//delete the file
				Path path = Paths.get(ServerMain.ORGANISATION_TOB_FOLDER + "/" + organisation.getTermsOfBusiness());
				Files.delete(path);
			} catch (IOException e) {
				//TODO NEXT: revert here
				e.printStackTrace();
				return false;
			}
		}

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM organisation WHERE organisation_id = ?");
			statement.setInt(1, organisation.getOrganisationId());
			returned = statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		if (returned != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean updateOrganisationDetails(Organisation organisation) {
		PreparedStatement statement = null;

		// add the candidate
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE organisation SET phone_number = ?, email_address = ?, website = ?, address = ? " + "WHERE organisation_id = ?");
			statement.setString(1, organisation.getPhoneNumber());
			statement.setString(2, organisation.getEmailAddress());
			statement.setString(3, organisation.getWebsite());
			statement.setString(4, organisation.getAddress());
			statement.setInt(5, organisation.getOrganisationId());
			int value = statement.executeUpdate();
			
			if(value == -1) {
				return false;
			}

			return true;
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
	}
	
	public Map<Organisation, Map<Boolean, Integer>> getOrganisationReport(Report report) {
		PreparedStatement statement = null;
		Date fromDate = new Date(report.getFromDate().getTime());
		Date toDate = new Date(report.getToDate().getTime());
		
		// setup the map
		Map<Organisation, Map<Boolean, Integer>> results = new HashMap<>();
		List<Organisation> organisations = DaoFactory.getOrganisationDao().getOrganisations();
		for(Organisation organisation : organisations) {
			results.put(organisation, new HashMap<Boolean, Integer>());
		}
		
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			for(Organisation organisation : organisations) {
				Map<Boolean, Integer> organisationMap = results.get(organisation);
				int openVacancies = 0, closedVacancies = 0;
				
				statement = conn.prepareStatement("SELECT vacancy_status, COUNT(*) as occurences FROM vacancy WHERE vacancy_date BETWEEN ? AND ? " +
						"GROUP BY vacancy_status, organisation_organisation_id HAVING organisation_organisation_id = ?");
				statement.setDate(1, fromDate);
				statement.setDate(2, toDate);
				statement.setInt(3, organisation.getOrganisationId());
				
				ResultSet rs = statement.executeQuery();
				while(rs.next()) {
					boolean status = rs.getBoolean("vacancy_status");
					
					if(status) {
						openVacancies = rs.getInt("occurences");
					} else {
						closedVacancies = rs.getInt("occurences");
					}
				}
				organisationMap.put(true, openVacancies);
				organisationMap.put(false, closedVacancies);
			}
			
		}  catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return results;
	}
}

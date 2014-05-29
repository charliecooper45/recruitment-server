package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import server.ServerMain;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

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
		String name = null, phoneNumber = null, emailAddress = null, website = null, address = null, termsOfBusiness = null, notes = null, linkedInProfile = null, userId = null;
		int id = -1;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT organisation_id, organisation_name, phone_number, email_address, website, " + "address, terms_of_business, notes, user_user_id FROM organisation WHERE organisation_id = ?");
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
			System.err.println("Path of organisation tob: " + path);
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
			statement.setInt(1, organisation.getId());
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
			statement = conn.prepareStatement("INSERT INTO organisation (organisation_name, phone_number, email_address, website, address, terms_of_business, " +
					"notes, user_user_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
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
}

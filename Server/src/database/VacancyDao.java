package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import server.ServerMain;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import database.beans.User;
import database.beans.Vacancy;

/**
 * Processes requests for the vacancy table in the recruitment database.
 * @author Charlie
 */
public class VacancyDao {
	public List<Vacancy> getVacancies(boolean open, User user) {
		PreparedStatement statement = null;
		List<Vacancy> vacancies = new ArrayList<>();
		int vacancyId = -1, organisationId = -1, contactId = -1;
		boolean vacancyStatus = false;
		Date date = null;
		String name = null, text = null, profile = null, userId = null, organisationName = null, contactName = null, contactPhoneNumber = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			// set the value for the arguments specified in the parameters
			String userType = "%";
			if (user != null)
				userType = user.getUserId();

			if (open) {
				// run this if only looking for open vacancies
				statement = conn.prepareStatement("SELECT vacancy_id, vacancy_status, name, vacancy_date, notes, profile, organisation_organisation_id, user_user_id, " + "contact_contact_id FROM vacancy WHERE user_user_id LIKE ? AND vacancy_status LIKE ?");
				statement.setString(1, userType);
				statement.setObject(2, open);
			} else {
				// run this if looking for all vacancies
				statement = conn.prepareStatement("SELECT vacancy_id, vacancy_status, name, vacancy_date, notes, profile, organisation_organisation_id, user_user_id, " + "contact_contact_id FROM vacancy WHERE user_user_id LIKE ?");
				statement.setString(1, userType);
			}

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				vacancyId = rs.getInt("vacancy_id");
				organisationId = rs.getInt("organisation_organisation_id");
				contactId = rs.getInt("contact_contact_id");
				vacancyStatus = rs.getBoolean("vacancy_status");
				date = rs.getDate("vacancy_date");
				name = rs.getString("name");
				text = rs.getString("notes");
				profile = rs.getString("profile");
				userId = rs.getString("user_user_id");

				PreparedStatement orgStatement = conn.prepareStatement("SELECT organisation_name FROM organisation WHERE organisation_id = ?");
				orgStatement.setInt(1, organisationId);
				ResultSet orgRs = orgStatement.executeQuery();
				if (orgRs.next()) {
					organisationName = orgRs.getString("organisation_name");
				} else {
					continue;
				}

				PreparedStatement conStatement = conn.prepareStatement("SELECT first_name, surname, phone_number FROM contact WHERE contact_id = ?");
				conStatement.setInt(1, contactId);
				ResultSet conRs = conStatement.executeQuery();
				if (conRs.next()) {
					contactName = conRs.getString("first_name") + " " + conRs.getString("surname");
					contactPhoneNumber = conRs.getString("phone_number");
				} else {
					continue;
				}

				Vacancy vacancy = new Vacancy(vacancyId, vacancyStatus, name, date, text, profile, organisationId, organisationName, userId, contactId, contactName, contactPhoneNumber);
				vacancies.add(vacancy);
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return vacancies;
	}

	public Vacancy getVacancy(int vacancyId) {
		PreparedStatement statement = null;
		Vacancy vacancy = null;
		int organisationId = -1, contactId = -1;
		boolean vacancyStatus = false;
		Date date = null;
		String name = null, text = null, profile = null, userId = null, organisationName = null, contactName = null, contactPhoneNumber = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT vacancy_id, vacancy_status, name, vacancy_date, notes, profile, organisation_organisation_id, user_user_id, " + "contact_contact_id FROM vacancy WHERE vacancy_id LIKE ?");
			statement.setInt(1, vacancyId);

			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				vacancyId = rs.getInt("vacancy_id");
				organisationId = rs.getInt("organisation_organisation_id");
				contactId = rs.getInt("contact_contact_id");
				vacancyStatus = rs.getBoolean("vacancy_status");
				date = rs.getDate("vacancy_date");
				name = rs.getString("name");
				text = rs.getString("notes");
				profile = rs.getString("profile");
				userId = rs.getString("user_user_id");

				PreparedStatement orgStatement = conn.prepareStatement("SELECT organisation_name FROM organisation WHERE organisation_id = ?");
				orgStatement.setInt(1, organisationId);
				ResultSet orgRs = orgStatement.executeQuery();
				if (orgRs.next()) {
					organisationName = orgRs.getString("organisation_name");
				} else {
					return null;
				}

				PreparedStatement conStatement = conn.prepareStatement("SELECT first_name, surname, phone_number FROM contact WHERE contact_id = ?");
				conStatement.setInt(1, contactId);
				ResultSet conRs = conStatement.executeQuery();
				if (conRs.next()) {
					contactPhoneNumber = conRs.getString("phone_number");
					contactName = conRs.getString("first_name") + " " + conRs.getString("surname");
				} else {
					return null;
				}

				vacancy = new Vacancy(vacancyId, vacancyStatus, name, date, text, profile, organisationId, organisationName, userId, contactId, contactName, contactPhoneNumber);
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return vacancy;
	}

	public RemoteInputStream getVacancyProfile(String fileName) {
		String fileLocation = ServerMain.VACANCY_PROFILES_FOLDER + "/" + fileName;
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

	public boolean addVacancyProfile(Vacancy vacancy, RemoteInputStream profileData, String oldFileName) {
		PreparedStatement statement = null;
		InputStream fileData;

		// add the profile to the folder
		try {
			//delete the old file if it exists
			Path path = Paths.get(ServerMain.VACANCY_PROFILES_FOLDER + "/" + oldFileName);
			if (Files.exists(path))
				Files.delete(path);

			// add the new file
			fileData = RemoteInputStreamClient.wrap(profileData);
			path = ServerMain.getCorrectFilePath(ServerMain.VACANCY_PROFILES_FOLDER, vacancy.getProfile());
			ServerMain.storeFile(fileData, path);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE vacancy SET profile = ? WHERE vacancy_id = ?");
			statement.setString(1, vacancy.getProfile());
			statement.setInt(2, vacancy.getVacancyId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean removeVacancyProfile(Vacancy vacancy) {
		PreparedStatement statement = null;

		try {
			//delete the file
			Path path = Paths.get(ServerMain.VACANCY_PROFILES_FOLDER + "/" + vacancy.getProfile());
			Files.delete(path);
		} catch (IOException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE vacancy SET profile = null WHERE vacancy_id = ?");
			statement.setInt(1, vacancy.getVacancyId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean changeVacancyStatus(Vacancy vacancy) {
		PreparedStatement statement = null;

		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE vacancy SET vacancy_status = ? WHERE vacancy_id = ?");
			statement.setBoolean(1, vacancy.getStatus());
			statement.setInt(2, vacancy.getVacancyId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean addVacancy(Vacancy vacancy, RemoteInputStream profileData) {
		PreparedStatement statement = null;
		InputStream fileData;
		Path path = null;

		if (profileData != null) {
			try {
				// add the new file if it has been specified
				fileData = RemoteInputStreamClient.wrap(profileData);
				path = ServerMain.getCorrectFilePath(ServerMain.VACANCY_PROFILES_FOLDER, vacancy.getProfile());
				ServerMain.storeFile(fileData, path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		// add the vacancy
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO vacancy (vacancy_status, name, vacancy_date, notes, profile, organisation_organisation_id, user_user_id, contact_contact_id) " + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setBoolean(1, vacancy.getStatus());
			statement.setString(2, vacancy.getName());
			statement.setObject(3, vacancy.getVacancyDate());
			statement.setString(4, vacancy.getText());
			statement.setString(5, vacancy.getProfile());
			statement.setInt(6, vacancy.getOrganisationId());
			statement.setString(7, vacancy.getUserId());
			statement.setInt(8, vacancy.getContactId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		return true;
	}
}

package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
			statement = conn.prepareStatement("SELECT vacancy_id, vacancy_status, name, vacancy_date, notes, profile, organisation_organisation_id, user_user_id, " 
					+ "contact_contact_id FROM vacancy WHERE vacancy_id LIKE ?");
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
}

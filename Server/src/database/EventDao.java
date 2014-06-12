package database;

import java.io.IOException;
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

import database.beans.Candidate;
import database.beans.Event;
import database.beans.EventType;
import database.beans.Vacancy;

/**
 * Processes requests for the event table in the recruitment database.
 * @author Charlie
 */
public class EventDao {
	public List<Event> getShortlist(int vacancyId) {
		Event event = null;
		List<Event> shortlist = null;
		PreparedStatement statement = null;
		PreparedStatement candidateStatement = null;
		int eventId, candidateId;
		Date eventDate;
		int eventTime;
		String userId, candidateFirstName, candidateSurname;
		EventType eventType;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT event_id, event_date, event_time, candidate_candidate_id, user_user_id, event_type_event_type_name " + "FROM event WHERE vacancy_vacancy_id = ? AND event_type_event_type_name = 'SHORTLIST'");
			statement.setInt(1, vacancyId);
			ResultSet rs = statement.executeQuery();
			shortlist = new ArrayList<>();

			while (rs.next()) {
				eventId = rs.getInt("event_id");
				candidateId = rs.getInt("candidate_candidate_id");
				eventDate = rs.getDate("event_date");
				userId = rs.getString("user_user_id");
				eventType = EventType.valueOf(rs.getString("event_type_event_type_name"));

				// get the first name and surname of the candidate
				candidateStatement = conn.prepareStatement("SELECT first_name, surname FROM candidate WHERE candidate_id = ?");
				candidateStatement.setInt(1, candidateId);
				ResultSet candidateRS = candidateStatement.executeQuery();

				if (candidateRS.next()) {
					candidateFirstName = candidateRS.getString("first_name");
					candidateSurname = candidateRS.getString("surname");
					Candidate candidate = new Candidate(candidateId, candidateFirstName, candidateSurname, null, -1, null, null, null, null, null, null, null, null);
					event = new Event(eventType, candidate, eventDate, userId, vacancyId);
					shortlist.add(event);
				}
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}

		return shortlist;
	}

	public boolean addCandidatesToShortlist(List<Candidate> candidates, Vacancy vacancy, String userId) {
		boolean candidateAdded = false;
		PreparedStatement statement = null;
		int count = 0;

		for (Candidate candidate : candidates) {
			try (Connection conn = DatabaseConnectionPool.getConnection()) {
				// check if the candidate is already shortlisted on this vacancy
				statement = conn.prepareStatement("SELECT COUNT(*) AS count FROM event WHERE candidate_candidate_id = ? AND vacancy_vacancy_id = ? AND event_type_event_type_name = 'SHORTLIST'");
				statement.setInt(1, candidate.getId());
				statement.setInt(2, vacancy.getVacancyId());
				ResultSet rs = statement.executeQuery();
				if (rs.next()) {
					count = rs.getInt("count");
				}

				if (count == 0) {
					// add the candidate to the shortlist as it is not present
					statement = conn.prepareStatement("INSERT INTO event (event_date, event_time, candidate_candidate_id, user_user_id, vacancy_vacancy_id, event_type_event_type_name) " + "VALUES (CURDATE(), CURTIME(), ?, ?, ?, 'SHORTLIST')");
					statement.setInt(1, candidate.getId());
					statement.setString(2, userId);
					statement.setInt(3, vacancy.getVacancyId());
					int value = statement.executeUpdate();
					if (value != 0) {
						candidateAdded = true;
					}
				}
				count = 0;
			} catch (SQLException e) {
				//TODO NEXT: Handle exceptions 
				e.printStackTrace();
			}
		}
		return candidateAdded;
	}

	public boolean removeCandidateFromShortlist(int candidateId, int vacancyId) {
		PreparedStatement statement = null;
		int returned = 0;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM event WHERE candidate_candidate_id = ? AND vacancy_vacancy_id = ? AND event_type_event_type_name = 'SHORTLIST'");
			statement.setInt(1, candidateId);
			statement.setInt(2, vacancyId);
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
}

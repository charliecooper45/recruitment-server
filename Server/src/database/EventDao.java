package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	public List<Event> getCandidateEvents(int candidateId) {
		PreparedStatement statement = null;
		List<Event> events = new ArrayList<>();
		Date date = null;
		Time time = null;
		EventType eventType = null;
		int vacancyId = -1;
		int eventId = -1;
		String vacancyName = null;
		String userId = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT event_id, event_date, event_time, event_type_event_type_name, user_user_id, vacancy_vacancy_id FROM event WHERE candidate_candidate_id = ?");
			statement.setInt(1, candidateId);
			
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				eventId = rs.getInt("event_id");
				date = rs.getDate("event_date");
				time = rs.getTime("event_time");
				eventType = EventType.valueOf(rs.getString("event_type_event_type_name"));
				vacancyId = rs.getInt("vacancy_vacancy_id");
				userId = rs.getString("user_user_id");
				
				// get the vacancy name
				PreparedStatement vacancyStatement = conn.prepareStatement("SELECT name FROM vacancy WHERE vacancy_id = ?");
				vacancyStatement.setInt(1, vacancyId);
				
				ResultSet vacancyRs = vacancyStatement.executeQuery();
				if(vacancyRs.next()) {
					vacancyName = vacancyRs.getString("name");
				}
				
				Event event = new Event(eventType, null, date, time, userId, vacancyId, vacancyName);
				event.setEventId(eventId);
				events.add(event);
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}

		return events;
	}

	public boolean addEvent(Event event) {
		PreparedStatement statement = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO event (event_date, event_time, candidate_candidate_id, user_user_id, vacancy_vacancy_id, event_type_event_type_name) " +
					"VALUES (?, ?, ?, ?, ?, ?)");
			statement.setDate(1, new java.sql.Date(event.getDate().getTime()));
			statement.setTime(2, event.getTime());
			statement.setInt(3, event.getCandidate().getId());
			statement.setString(4, event.getUserId());
			statement.setInt(5, event.getVacancyId());
			statement.setString(6, String.valueOf(event.getEventType()));

			int updated = statement.executeUpdate();
			if(updated == 0) {
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean removeEvent(int eventId) {
		PreparedStatement statement = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM event WHERE event_id = ?");
			statement.setInt(1, eventId);

			int updated = statement.executeUpdate();
			if(updated == 0) {
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

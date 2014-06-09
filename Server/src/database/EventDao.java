package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import database.beans.Candidate;
import database.beans.Event;
import database.beans.EventType;

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
					Candidate candidate = new Candidate(candidateId, candidateFirstName, candidateSurname, null, null, null, null, null, null, null, null);
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
}

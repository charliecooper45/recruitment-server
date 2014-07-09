package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import database.beans.Candidate;
import database.beans.Event;
import database.beans.EventType;
import database.beans.Report;
import database.beans.User;
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
					event = new Event(eventType, candidate, eventDate, null, userId, new Vacancy(vacancyId, null));
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
			while (rs.next()) {
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
				if (vacancyRs.next()) {
					vacancyName = vacancyRs.getString("name");
				}

				Event event = new Event(eventType, null, date, time, userId, new Vacancy(vacancyId, vacancyName));
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

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO event (event_date, event_time, candidate_candidate_id, user_user_id, vacancy_vacancy_id, event_type_event_type_name) " + "VALUES (?, ?, ?, ?, ?, ?)");
			statement.setDate(1, new java.sql.Date(event.getDate().getTime()));
			statement.setTime(2, event.getTime());
			statement.setInt(3, event.getCandidate().getId());
			statement.setString(4, event.getUserId());
			statement.setInt(5, event.getVacancyId());
			statement.setString(6, String.valueOf(event.getEventType()));

			int updated = statement.executeUpdate();
			if (updated == 0) {
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

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM event WHERE event_id = ?");
			statement.setInt(1, eventId);

			int updated = statement.executeUpdate();
			if (updated == 0) {
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public List<Event> getEvents(boolean shortlist, boolean cvSent, boolean interview, boolean placement, boolean user, String userId) {
		Event event = null;
		List<Event> events = new ArrayList<>();
		PreparedStatement statement = null;
		String userType = "%";

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT first_name, surname, event_type_event_type_name, event_date, event.user_user_id, name, organisation_name, candidate_id " + "FROM (event INNER JOIN candidate ON candidate_candidate_id = candidate_id) INNER JOIN " + "(vacancy INNER JOIN organisation ON organisation_organisation_id = organisation_id) ON vacancy_vacancy_id = vacancy_id " + "WHERE event.user_user_id LIKE ? AND vacancy_status = true");
			if (user) {
				userType = userId;
			}
			statement.setString(1, userType);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String firstName = rs.getString("first_name");
				String surname = rs.getString("surname");
				EventType eventType = EventType.valueOf(rs.getString("event_type_event_type_name"));
				Date date = rs.getDate("event_date");
				String eventUserId = rs.getString("event.user_user_id");
				String vacancyName = rs.getString("name");
				String organisationName = rs.getString("organisation_name");
				int candidateId = rs.getInt("candidate_id");

				Candidate candidate = new Candidate(candidateId, firstName, surname, null, -1, null, null, null, null, null, null, null, null);
				Vacancy vacancy = new Vacancy(-1, false, vacancyName, null, null, null, -1, organisationName, null, -1, null, null);
				event = new Event(eventType, candidate, date, null, eventUserId, vacancy);
				events.add(event);
			}

			// remove any events that are not meant to be in the list
			Iterator<Event> iterator = events.iterator();
			while (iterator.hasNext()) {
				Event currentEvent = iterator.next();

				if (!shortlist) {
					if (currentEvent.getEventType() == EventType.SHORTLIST) {
						iterator.remove();
					}
				}
				if (!cvSent) {
					if (currentEvent.getEventType() == EventType.CV_SENT) {
						iterator.remove();
					}
				}
				if (!interview) {
					if (currentEvent.getEventType() == EventType.PHONE_INTERVIEW) {
						iterator.remove();
					} else if (currentEvent.getEventType() == EventType.INTERVIEW_1) {
						iterator.remove();
					} else if (currentEvent.getEventType() == EventType.INTERVIEW_2) {
						iterator.remove();
					} else if (currentEvent.getEventType() == EventType.INTERVIEW_3) {
						iterator.remove();
					} else if (currentEvent.getEventType() == EventType.INTERVIEW_4) {
						iterator.remove();
					} else if (currentEvent.getEventType() == EventType.FINAL_INTERVIEW) {
						iterator.remove();
					}
				}
				if (!placement) {
					if (currentEvent.getEventType() == EventType.PLACEMENT) {
						iterator.remove();
					}
				}
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return events;
	}

	public Map<User, Map<EventType, Integer>> getUserReport(Report report) {
		PreparedStatement statement = null;
		Date fromDate = new Date(report.getFromDate().getTime());
		Date toDate = new Date(report.getToDate().getTime());

		// setup the map
		Map<User, Map<EventType, Integer>> results = new HashMap<>();
		List<User> users = DaoFactory.getUserDao().getUsers(null, true);
		for (User user : users) {
			results.put(user, new HashMap<EventType, Integer>());
		}

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			for (User user : users) {
				Map<EventType, Integer> userMap = results.get(user);

				int cvsSent = 0, shortlists = 0, phoneInterviews = 0, interview1s = 0, interview2s = 0, interview3s = 0, interview4s = 0, finalInterviews = 0, placements = 0;
				statement = conn.prepareStatement("SELECT event_type_event_type_name, COUNT(event_type_event_type_name) AS occurences FROM event " + "WHERE user_user_id = ? AND (event_date BETWEEN ? AND ?) " + "GROUP BY event_type_event_type_name, event_date");
				statement.setString(1, user.getUserId());
				statement.setDate(2, fromDate);
				statement.setDate(3, toDate);

				ResultSet rs = statement.executeQuery();
				while (rs.next()) {
					EventType eventType = EventType.valueOf(rs.getString("event_type_event_type_name"));

					if (eventType == EventType.CV_SENT) {
						cvsSent = rs.getInt("occurences");
					}
					if (eventType == EventType.SHORTLIST) {
						shortlists = rs.getInt("occurences");
					}
					if (eventType == EventType.PHONE_INTERVIEW) {
						phoneInterviews = rs.getInt("occurences");
					}
					if (eventType == EventType.INTERVIEW_1) {
						interview1s = rs.getInt("occurences");
					}
					if (eventType == EventType.INTERVIEW_2) {
						interview2s = rs.getInt("occurences");
					}
					if (eventType == EventType.INTERVIEW_3) {
						interview3s = rs.getInt("occurences");
					}
					if (eventType == EventType.INTERVIEW_4) {
						interview4s = rs.getInt("occurences");
					}
					if (eventType == EventType.FINAL_INTERVIEW) {
						finalInterviews = rs.getInt("occurences");
					}
					if (eventType == EventType.PLACEMENT) {
						placements = rs.getInt("occurences");
					}
				}

				userMap.put(EventType.CV_SENT, cvsSent);
				userMap.put(EventType.SHORTLIST, shortlists);
				userMap.put(EventType.PHONE_INTERVIEW, phoneInterviews);
				userMap.put(EventType.INTERVIEW_1, interview1s);
				userMap.put(EventType.INTERVIEW_2, interview2s);
				userMap.put(EventType.INTERVIEW_3, interview3s);
				userMap.put(EventType.INTERVIEW_4, interview4s);
				userMap.put(EventType.FINAL_INTERVIEW, finalInterviews);
				userMap.put(EventType.PLACEMENT, placements);
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}

		return results;
	}

	public Map<Vacancy, Map<EventType, Integer>> getVacancyReport(Report report) {
		PreparedStatement statement = null;
		Date fromDate = new Date(report.getFromDate().getTime());
		Date toDate = new Date(report.getToDate().getTime());

		// setup the map
		Map<Vacancy, Map<EventType, Integer>> results = new HashMap<>();
		List<Vacancy> vacancies = DaoFactory.getVacancyDao().getVacancies(fromDate, toDate);
		for (Vacancy vacancy : vacancies) {
			results.put(vacancy, new HashMap<EventType, Integer>());
		}

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			for (Vacancy vacancy : vacancies) {
				Map<EventType, Integer> vacancyMap = results.get(vacancy);
				int cvsSent = 0, shortlists = 0, phoneInterviews = 0, interview1s = 0, interview2s = 0, interview3s = 0, interview4s = 0, finalInterviews = 0, placements = 0;

				statement = conn.prepareStatement("SELECT event_type_event_type_name, COUNT(event_type_event_type_name) AS occurences FROM event WHERE vacancy_vacancy_id = ? " +
						"GROUP BY event_type_event_type_name");
				statement.setInt(1, vacancy.getVacancyId());
				
				ResultSet rs = statement.executeQuery();
				while(rs.next()) {
					EventType eventType = EventType.valueOf(rs.getString("event_type_event_type_name"));
					
					if(eventType == EventType.CV_SENT) {
						cvsSent = rs.getInt("occurences");
					}
					if(eventType == EventType.SHORTLIST) {
						shortlists = rs.getInt("occurences");
					}
					if(eventType == EventType.PHONE_INTERVIEW) {
						phoneInterviews = rs.getInt("occurences");
					}
					if(eventType == EventType.INTERVIEW_1) {
						interview1s = rs.getInt("occurences");
					}
					if(eventType == EventType.INTERVIEW_2) {
						interview2s = rs.getInt("occurences");
					}
					if(eventType == EventType.INTERVIEW_3) {
						interview3s = rs.getInt("occurences");
					}
					if(eventType == EventType.INTERVIEW_4) {
						interview4s = rs.getInt("occurences");
					}
					if(eventType == EventType.FINAL_INTERVIEW) {
						finalInterviews = rs.getInt("occurences");
					}
					if(eventType == EventType.PLACEMENT) {
						placements = rs.getInt("occurences");
					}
				}
				
				vacancyMap.put(EventType.CV_SENT, cvsSent);
				vacancyMap.put(EventType.SHORTLIST, shortlists);
				vacancyMap.put(EventType.PHONE_INTERVIEW, phoneInterviews);
				vacancyMap.put(EventType.INTERVIEW_1, interview1s);
				vacancyMap.put(EventType.INTERVIEW_2, interview2s);
				vacancyMap.put(EventType.INTERVIEW_3, interview3s);
				vacancyMap.put(EventType.INTERVIEW_4, interview4s);
				vacancyMap.put(EventType.FINAL_INTERVIEW, finalInterviews);
				vacancyMap.put(EventType.PLACEMENT, placements);
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}

		return results;
	}
}

package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.beans.Candidate;

/**
 * Processes requests for the candidate table in the recruitment database.
 * @author Charlie
 */
public class CandidateDao {
	public List<Candidate> getCandidates() {	
		List<Candidate> candidates = new ArrayList<>();
		int id = -1;
		String firstName, surname, jobTitle, phoneNumber, emailAddress, address, notes, linkedInProfile, cv, userId;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT candidate_id, first_name, surname, job_title, phone_number, email_address, address, notes, linkedin_profile, cv, " +
					"user_user_id FROM candidate");
			while (rs.next()) {
				id = rs.getInt("candidate_id");
				firstName = rs.getString("first_name");
				surname = rs.getString("surname");
				jobTitle = rs.getString("job_title");
				phoneNumber = rs.getString("phone_number");
				emailAddress = rs.getString("email_address");
				address = rs.getString("address");
				notes = rs.getString("notes");
				linkedInProfile = rs.getString("linkedin_profile");
				cv = rs.getString("address");
				userId = rs.getString("user_user_id");
				
				candidates.add(new Candidate(id, firstName, surname, jobTitle, phoneNumber, emailAddress, address, notes, linkedInProfile, cv, userId));
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return candidates;
	}
}

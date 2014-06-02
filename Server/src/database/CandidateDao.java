package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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

	public boolean addCandidate(Candidate candidate, RemoteInputStream cvData) {
		PreparedStatement statement = null;
		InputStream fileData;
		Path path = null;

		if (cvData != null) {
			try {
				// add the new file if it has been specified
				fileData = RemoteInputStreamClient.wrap(cvData);
				path = ServerMain.getCorrectFilePath(ServerMain.CANDIDATE_CV_FOLDER, candidate.getCV());
				candidate.setCV(path.getFileName().toString());
				ServerMain.storeFile(fileData, path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		// add the candidate
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO candidate (first_name, surname, job_title, phone_number, email_address, address, notes, linkedin_profile, cv, user_user_id)" +
					" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, candidate.getFirstName());
			statement.setString(2, candidate.getSurname());
			statement.setString(3, candidate.getJobTitle());
			statement.setString(4, candidate.getPhoneNumber());
			statement.setString(5, candidate.getEmailAddress());
			statement.setString(6, candidate.getAddress());
			statement.setString(7, candidate.getNotes());
			statement.setString(8, candidate.getLinkedInProfile());
			statement.setString(9, candidate.getCV());
			statement.setString(10, candidate.getUserId());
			int value = statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		return true;
	}
}

package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import database.beans.Candidate;
import database.beans.Organisation;
import database.beans.Search;
import database.beans.Skill;

/**
 * Processes requests for the candidate table in the recruitment database.
 * @author Charlie
 */
public class CandidateDao {
	public List<Candidate> getCandidates() {
		List<Candidate> candidates = new ArrayList<>();
		int id = -1;
		String firstName, surname, jobTitle, phoneNumber, emailAddress, address, notes, linkedInProfile, cv, userId;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT candidate_id, first_name, surname, job_title, phone_number, email_address, address, notes, linkedin_profile, cv, " + "user_user_id FROM candidate");
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
				cv = rs.getString("cv");
				userId = rs.getString("user_user_id");

				candidates.add(new Candidate(id, firstName, surname, jobTitle, -1, null, phoneNumber, emailAddress, address, notes, linkedInProfile, cv, userId));
			}
		} catch (SQLException e) {
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
			statement = conn.prepareStatement("INSERT INTO candidate (first_name, surname, job_title, phone_number, email_address, address, notes, linkedin_profile, cv, user_user_id)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
			
			if(value == 0) {
				return false;
			}

			// add the organisation if the candidate has one
			if (candidate.getOrganisationId() != -1) {
				ResultSet rs = statement.getGeneratedKeys();
				if (rs.next()) {
					DaoFactory.getCandidateWorksAtDao().addEmployment(rs.getInt(1), candidate.getOrganisationId());
				}
			}
		} catch (SQLException e) {
			//TODO NEXT: revert here, this is currently adding a candidate regardless
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean removeCandidate(Candidate candidate) {
		PreparedStatement statement = null;
		int returned = 0;

		// remove the candidate CV if it is present
		if (candidate.getCV() != null) {
			try {
				//delete the file
				Path path = Paths.get(ServerMain.CANDIDATE_CV_FOLDER + "/" + candidate.getCV());
				Files.delete(path);
			} catch (IOException e) {
				//TODO NEXT: revert here
				e.printStackTrace();
				return false;
			}
		}

		// remove the candidate record
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM candidate WHERE candidate_id = ?");
			statement.setInt(1, candidate.getId());
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

	public List<Candidate> searchCandidates(Search search) {
		PreparedStatement statement = null;
		List<Candidate> candidates = new ArrayList<>();
		int id = -1;
		String firstName, surname, jobTitle, phoneNumber, emailAddress, address, notes, linkedInProfile, cv, userId;

		// configure the name for searching
		String name = search.getName();
		String searchFirstName = ".";
		String searchSurname = ".";
		String[] names = name.split(" ");
		if (!names[0].trim().isEmpty()) {
			searchFirstName = names[0];
			if (names.length > 1) {
				if (!names[1].trim().isEmpty()) {
					searchSurname = names[1];
				} else {
					searchSurname = null;
				}
			} else {
				searchSurname = names[0];
			}
		}

		// configure the job title for searching
		String searchJobTitle = search.getJobTitle();
		if (searchJobTitle.trim().isEmpty()) {
			searchJobTitle = null;
		}

		// configure the skills for searching
		String[] searchSkills = { null, null, null, null, null };
		List<Skill> skills = new ArrayList<>(search.getSkills());

		for (int i = 0; i < skills.size(); i++) {
			searchSkills[i] = skills.get(i).toString();
		}

		try (Connection conn = DatabaseConnectionPool.getConnection()) {

			// these queries are run if there are no skills to search for
			if (searchJobTitle == null) {
				statement = conn.prepareStatement("SELECT candidate_id, first_name, surname, job_title, phone_number, email_address, address, notes, linkedin_profile, cv, " + "user_user_id FROM candidate WHERE (first_name REGEXP ? OR surname REGEXP ?)");
				statement.setString(1, searchFirstName + "+");
				statement.setString(2, searchSurname + "+");
			} else {
				statement = conn.prepareStatement("SELECT candidate_id, first_name, surname, job_title, phone_number, email_address, address, notes, linkedin_profile, cv, " + "user_user_id FROM candidate WHERE (first_name REGEXP ? OR surname REGEXP ?) AND (job_title REGEXP ?)");
				statement.setString(1, searchFirstName + "+");
				statement.setString(2, searchSurname + "+");
				statement.setString(3, searchJobTitle + "+");
			}

			ResultSet rs = statement.executeQuery();
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
				cv = rs.getString("cv");
				userId = rs.getString("user_user_id");

				Candidate candidate = new Candidate(id, firstName, surname, jobTitle, -1, null, phoneNumber, emailAddress, address, notes, linkedInProfile, cv, userId);

				if (skills.isEmpty()) {
					// no need to check for the skill so add it to the list
					candidates.add(candidate);
				} else {
					// check if the user has the required skills
					//TODO NEXT: build queries for when the search includes skills for the candidates
					boolean hasASkill = false;

					for (Skill skill : skills) {
						// check the candidate has all the skills
						hasASkill = DaoFactory.getCandidateHasSkillDao().candidateHasSkill(candidate, skill);
						if (!hasASkill) {
							break;
						}
					}

					if (hasASkill) {
						candidates.add(candidate);
					}
				}
			}

		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}

		return candidates;
	}

	public Candidate getCandidate(int candidateId) {
		PreparedStatement statement;
		Candidate candidate = null;
		String firstName = null, surname = null, jobTitle = null, phoneNumber = null, emailAddress = null, address = null, notes = null, linkedInProfile = null, cv = null, userId = null;
		int id = -1;
		int organisationId = -1;
		String organisationName = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT candidate_id, first_name, surname, job_title, phone_number, email_address, address, notes, linkedin_profile, " + "cv, user_user_id FROM candidate WHERE candidate_id = ?");
			statement.setInt(1, candidateId);

			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				id = rs.getInt("candidate_id");
				firstName = rs.getString("first_name");
				surname = rs.getString("surname");
				jobTitle = rs.getString("job_title");
				phoneNumber = rs.getString("phone_number");
				emailAddress = rs.getString("email_address");
				address = rs.getString("address");
				notes = rs.getString("notes");
				linkedInProfile = rs.getString("linkedin_profile");
				cv = rs.getString("cv");
				userId = rs.getString("user_user_id");
			}

			// check if the candidate works at an organisation and if they do add this information to the Candidate object
			Organisation organisation = DaoFactory.getCandidateWorksAtDao().getCandididateOrganisation(id);
			if (organisation != null) {
				organisationId = organisation.getOrganisationId();
				organisationName = organisation.getOrganisationName();
			}

			candidate = new Candidate(id, firstName, surname, jobTitle, organisationId, organisationName, phoneNumber, emailAddress, address, notes, linkedInProfile, cv, userId);
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return candidate;
	}

	public RemoteInputStream getCandidateCV(String fileName) {
		String fileLocation = ServerMain.CANDIDATE_CV_FOLDER + "/" + fileName;
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

	public boolean addLinkedInProfile(Candidate candidate, URL profileURL) {
		PreparedStatement statement;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE candidate SET linkedin_profile = ? WHERE candidate_id = ?");
			statement.setString(1, profileURL.toString());
			statement.setInt(2, candidate.getId());

			int i = statement.executeUpdate();
			if (i == 0) {
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean removeLinkedInProfile(Candidate candidate) {
		PreparedStatement statement;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE candidate SET linkedin_profile = null WHERE candidate_id = ?");
			statement.setInt(1, candidate.getId());

			int i = statement.executeUpdate();
			if (i == 0) {
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean addCandidateCv(Candidate candidate, RemoteInputStream remoteFileData, String oldFileName) {
		PreparedStatement statement = null;
		InputStream fileData;

		// add the cv to the folder
		try {
			//delete the old file if it exists
			Path path = Paths.get(ServerMain.CANDIDATE_CV_FOLDER + "/" + oldFileName);
			if (Files.exists(path))
				Files.delete(path);

			// add the new file
			fileData = RemoteInputStreamClient.wrap(remoteFileData);
			path = ServerMain.getCorrectFilePath(ServerMain.CANDIDATE_CV_FOLDER, candidate.getCV());
			ServerMain.storeFile(fileData, path);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE candidate SET cv = ? WHERE candidate_id = ?");
			statement.setString(1, candidate.getCV());
			statement.setInt(2, candidate.getId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean removeCandidateCv(Candidate candidate) {
		PreparedStatement statement = null;

		try {
			//delete the file
			Path path = Paths.get(ServerMain.CANDIDATE_CV_FOLDER + "/" + candidate.getCV());
			Files.delete(path);
		} catch (IOException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE candidate SET cv = null WHERE candidate_id = ?");
			statement.setInt(1, candidate.getId());
			statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean updateCandidateDetails(Candidate candidate) {
		PreparedStatement statement = null;

		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE candidate SET job_title = ?, phone_number = ?, email_address = ?, address = ? " + "WHERE candidate_id = ?");
			statement.setString(1, candidate.getJobTitle());
			statement.setString(2, candidate.getPhoneNumber());
			statement.setString(3, candidate.getEmailAddress());
			statement.setString(4, candidate.getAddress());
			statement.setInt(5, candidate.getId());
			int value = statement.executeUpdate();
			
			if(value == 0) {
				return false;
			}

			// delete any existing record then insert the new record to show where the candidate works
			DaoFactory.getCandidateWorksAtDao().removeEmployment(candidate.getId());

			if (candidate.getOrganisationId() != -1) {
				DaoFactory.getCandidateWorksAtDao().addEmployment(candidate.getId(), candidate.getOrganisationId());
			}

			return true;
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
	}

	public boolean saveCandidateNotes(int candidateId, String notes) {
		PreparedStatement statement = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("UPDATE candidate SET notes = ? WHERE candidate_id = ?");
			statement.setString(1, notes);
			statement.setInt(2, candidateId);
			
			int result = statement.executeUpdate();
			
			if(result == 0) {
				return false;
			}
		}  catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

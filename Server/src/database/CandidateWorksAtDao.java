package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.beans.Organisation;

/**
 * Processes requests for the candidate_works_at table in the recruitment database.
 * @author Charlie
 */
public class CandidateWorksAtDao {

	public Organisation getCandididateOrganisation(int id) {
		PreparedStatement statement;
		
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT organisation_organisation_id FROM candidate_works_at WHERE candidate_candidate_id = ?");
			statement.setInt(1, id);

			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int organisationId = rs.getInt("organisation_organisation_id");
				return DaoFactory.getOrganisationDao().getOrganisation(organisationId);
			} else {
				return null;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
	}
	
	public int removeEmployment(int candidateId) {
		PreparedStatement statement;
		
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM candidate_works_at WHERE candidate_candidate_id = ?");
			statement.setInt(1, candidateId);
			return statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return 0;
		}
	}
	
	public int addEmployment(int candidateId, int organisationId) {
		PreparedStatement statement;
		
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO candidate_works_at (candidate_candidate_id, organisation_organisation_id) VALUES (?, ?)");
			statement.setInt(1, candidateId);
			statement.setInt(2, organisationId);
			return statement.executeUpdate();
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return 0;
		}
	}
}

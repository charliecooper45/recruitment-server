package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.beans.Candidate;
import database.beans.Skill;

/**
 * Processes requests for the candidate_has_skill table in the recruitment database.
 * @author Charlie
 */
public class CandidateHasSkillDao {
	public boolean candidateHasSkill(Candidate candidate, Skill skill) {
		PreparedStatement statement = null;

		// check if the candidate has the skill
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT COUNT(*) as count FROM candidate_has_skill WHERE candidate_candidate_id = ? AND skill_skill_name = ?");
			statement.setInt(1, candidate.getId());
			statement.setString(2, skill.getSkillName());
			statement.execute();
			
			ResultSet rs = statement.getResultSet();
			
			if(rs.next()) {
				if(rs.getInt("count") == 1) {
					return true;
				} else {
					return false;
				}
			}
			
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}

		return false;
	}
}

package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.beans.Candidate;
import database.beans.CandidateSkill;
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
	
	public List<CandidateSkill> getCandidateSkills(int candidateId) {
		PreparedStatement statement = null;
		List<CandidateSkill> candidateSkills = new ArrayList<>();
		String skillName;
		String userId;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT skill_skill_name, user_user_id FROM candidate_has_skill WHERE candidate_candidate_id = ?");
			statement.setInt(1, candidateId);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next()) {
				skillName = rs.getString("skill_skill_name");
				userId = rs.getString("user_user_id");
				candidateSkills.add(new CandidateSkill(candidateId, skillName, userId));
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		
		return candidateSkills;
	}

	public boolean addSkillToCandidate(Skill skill, Candidate candidate, String userId) {
		PreparedStatement statement = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO candidate_has_skill (candidate_candidate_id, skill_skill_name, user_user_id) VALUES (?,?,?)");
			statement.setInt(1, candidate.getId());
			statement.setString(2, skill.getSkillName());
			statement.setString(3, userId);
			int result = statement.executeUpdate();
			
			if(result != 0) {
				return true;
			}
			
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	public boolean removeSkillFromCandidate(Skill skill, Candidate candidate) {
		PreparedStatement statement = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM candidate_has_skill WHERE candidate_candidate_id = ? AND skill_skill_name = ?");
			statement.setInt(1, candidate.getId());
			statement.setString(2, skill.getSkillName());
			int result = statement.executeUpdate();
			
			if(result != 0) {
				return true;
			}
			
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
}

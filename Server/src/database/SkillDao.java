package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.beans.Skill;

/**
 * Processes requests for the skill table in the recruitment database.
 * @author Charlie
 */
public class SkillDao {
	public List<Skill> getSkills() {
		List<Skill> skills = new ArrayList<>();
		String skillName, userId;
		int usage = 0;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT skill_name, user_user_id FROM skill");
			while(rs.next()) {
				skillName = rs.getString("skill_name");
				userId = rs.getString("user_user_id");
				
				PreparedStatement usageStatement = conn.prepareStatement("SELECT COUNT(*) as count FROM candidate_has_skill GROUP BY skill_skill_name HAVING skill_skill_name = ?");
				usageStatement.setString(1, skillName);
				ResultSet usageRs = usageStatement.executeQuery();
				
				if(usageRs.next()) {
					usage = usageRs.getInt("count");
				}
				
				skills.add(new Skill(skillName, userId, usage));
				usage = 0;
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return skills;
	}

	public boolean addSkill(Skill skill) {
		PreparedStatement statement = null;
		
		// update the database
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO skill (skill_name, user_user_id) VALUES (?, ?)");
			statement.setString(1, skill.getSkillName());
			statement.setString(2, skill.getUserId());
			int updated = statement.executeUpdate();
			
			if(updated != 0) {
				return true;
			}
		} catch (SQLException e) {
			//TODO NEXT: revert here
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public boolean removeSkill(Skill skill) {
		PreparedStatement statement = null;
		int returned = 0;

		// remove the skill record
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM skill WHERE skill_name = ?");
			statement.setString(1, skill.getSkillName());
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

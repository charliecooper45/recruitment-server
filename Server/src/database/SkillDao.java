package database;

import java.sql.Connection;
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
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT skill_name, user_user_id FROM skill");
			while(rs.next()) {
				skillName = rs.getString("skill_name");
				userId = rs.getString("user_user_id");
				
				skills.add(new Skill(skillName, userId));
			}
		} catch (SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		return skills;
	}
}

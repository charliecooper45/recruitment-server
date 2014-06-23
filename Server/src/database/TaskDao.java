package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.beans.Task;

/**
 * Processes requests for the task table in the recruitment database.
 * @author Charlie
 */
public class TaskDao {
	public List<Task> getTasks(String userId) {
		List<Task> tasks = new ArrayList<>();
		PreparedStatement statement = null;
		
		try(Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("SELECT task_date, task_time, description FROM task WHERE user_user_id = ?");
			statement.setString(1, userId);
			
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				Date date = rs.getDate("task_date");
				String description = rs.getString("description");
				Date time = new Date(rs.getTime("task_time").getTime());
				Task task = new Task(date, time, description, userId);
				tasks.add(task);
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		
		return tasks;
	}
}

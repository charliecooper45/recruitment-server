package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
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
			statement = conn.prepareStatement("SELECT task_id, task_date, task_time, description FROM task WHERE user_user_id = ?");
			statement.setString(1, userId);
			
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("task_id");
				Date date = rs.getDate("task_date");
				String description = rs.getString("description");
				Date time = new Date(rs.getTime("task_time").getTime());
				Task task = new Task(id, date, time, description, userId);
				tasks.add(task);
			}
		} catch(SQLException e) {
			//TODO NEXT: Handle exceptions 
			e.printStackTrace();
			return null;
		}
		
		return tasks;
	}

	public boolean addTask(Task task) {
		PreparedStatement statement = null;

		// add the task
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("INSERT INTO task (task_date, task_time, description, user_user_id) VALUES (?, ?, ?, ?)");
			statement.setDate(1, new Date(task.getDate().getTime()));
			statement.setTime(2, new Time(task.getTime().getTime()));
			statement.setString(3, task.getDescription());
			statement.setString(4, task.getUserId());

			int value = statement.executeUpdate();
			
			if(value == 0) {
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: revert here, this is currently adding a task regardless
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean removeTask(Task task) {
		PreparedStatement statement = null;

		// add the task
		try (Connection conn = DatabaseConnectionPool.getConnection()) {
			statement = conn.prepareStatement("DELETE FROM task WHERE task_id = ?");
			statement.setInt(1, task.getTaskId());

			int value = statement.executeUpdate();
			
			if(value == 0) {
				System.err.println("here " + task.getTaskId());
				return false;
			}
		} catch (SQLException e) {
			//TODO NEXT: revert here, this is currently adding a task regardless
			e.printStackTrace();
			return false;
		}

		return true;
	}
}

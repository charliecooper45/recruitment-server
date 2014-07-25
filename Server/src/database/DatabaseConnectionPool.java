package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * Holds a cache of database connections that can be used by the client.
 * @author Charlie
 */
public class DatabaseConnectionPool {
	private static final DataSource DATA_SOURCE;
	private static final String URL;
	private static final String USERNAME;
	private static final String PASSWORD;

	static {
		Properties p = new Properties(System.getProperties());
		p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
		p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); 
		System.setProperties(p);
		
		URL = "jdbc:mysql://localhost:3306/recruitment_test_database";
		USERNAME = "root";
		PASSWORD = "letmein";

		DATA_SOURCE = init();
	}
	
	private static DataSource init() {
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setJdbcUrl(URL);
		cpds.setUser(USERNAME);
		cpds.setPassword(PASSWORD);
		cpds.setMinPoolSize(10);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(40);
		return cpds;
	}
	
	public static Connection getConnection() throws SQLException {
		return DATA_SOURCE.getConnection();
	}
	
	public static void destroy() {
		try {
			DataSources.destroy(DATA_SOURCE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

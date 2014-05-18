package database;

/**
 * Returns all DAO instances.
 * @author Charlie
 */
public final class DaoFactory {
	public static CandidateDao getCandidateDao() {
		return new CandidateDao();
	}
	
	public static UserDao getUserDao() {
		return new UserDao();
	}
}

package database;

import interfaces.ServerInterface;


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
	
	public static VacancyDao getVacancyDao() {
		return new VacancyDao();
	}

	public static OrganisationDao getOrganisationDao() {
		return new OrganisationDao();
	}

	public static ContactDao getContactDao() {
		return new ContactDao();
	}

	public static SkillDao getSkillDao() {
		return new SkillDao();
	}
}

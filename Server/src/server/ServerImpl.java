package server;

import interfaces.ServerInterface;
import interfaces.UserType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.healthmarketscience.rmiio.RemoteInputStream;

import database.DaoFactory;
import database.beans.Candidate;
import database.beans.Contact;
import database.beans.Organisation;
import database.beans.User;
import database.beans.Vacancy;

/**
 * Processes requests from the client. 
 * @author Charlie
 */
public class ServerImpl extends UnicastRemoteObject implements ServerInterface {
	protected ServerImpl() throws RemoteException {
		super();
	}

	@Override
	public List<Candidate> getCandidates() {
		return DaoFactory.getCandidateDao().getCandidates();
	}

	@Override
	public List<Vacancy> getVacancies(boolean open, User user) {
		return DaoFactory.getVacancyDao().getVacancies(open, user);
	}
	
	@Override
	public Vacancy getVacancy(int vacancyId) throws RemoteException {
		return DaoFactory.getVacancyDao().getVacancy(vacancyId);
	}

	@Override
	public List<User> getUsers(UserType userType, boolean status) throws RemoteException {
		return DaoFactory.getUserDao().getUsers(userType, status);
	}

	@Override
	public UserType getUserType(String userId) throws RemoteException {
		return DaoFactory.getUserDao().getUserType(userId);
	}

	@Override
	public RemoteInputStream getVacancyProfile(String fileName) {
		return DaoFactory.getVacancyDao().getVacancyProfile(fileName);
	}

	@Override
	public boolean addVacancyProfile(Vacancy vacancy, RemoteInputStream profileData, String oldFileName) throws RemoteException {
		return DaoFactory.getVacancyDao().addVacancyProfile(vacancy, profileData, oldFileName);
	}

	@Override
	public boolean removeVacancyProfile(Vacancy vacancy) throws RemoteException {
		return DaoFactory.getVacancyDao().removeVacancyProfile(vacancy);
	}

	@Override
	public boolean changeVacancyStatus(Vacancy vacancy) throws RemoteException {
		return DaoFactory.getVacancyDao().changeVacancyStatus(vacancy);
	}

	@Override
	public List<Organisation> getOrganisations() throws RemoteException {
		return DaoFactory.getOrganisationDao().getOrganisations();
	}

	@Override
	public List<Contact> getOrganisationsContacts(Organisation organisation) throws RemoteException {
		return DaoFactory.getContactDao().getOrganisationsContacts(organisation);
	}

	@Override
	public boolean addVacancy(Vacancy vacancy, RemoteInputStream profileData) throws RemoteException {
		return DaoFactory.getVacancyDao().addVacancy(vacancy, profileData);
	}

	@Override
	public boolean removeVacancy(Vacancy vacancy) throws RemoteException {
		return DaoFactory.getVacancyDao().removeVacancy(vacancy);
	}

	@Override
	public Organisation getOrganisation(int organisationId) throws RemoteException {
		return DaoFactory.getOrganisationDao().getOrganisation(organisationId);
	}
}

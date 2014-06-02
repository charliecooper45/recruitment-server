package server;

import interfaces.ServerInterface;
import interfaces.UserType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.healthmarketscience.rmiio.RemoteInputStream;

import database.beans.Candidate;
import database.beans.Contact;
import database.beans.Organisation;
import database.beans.User;
import database.beans.Vacancy;


/**
 * Controls access to the server via the proxy pattern.
 * @author Charlie
 */
public class ServerProxy extends UnicastRemoteObject implements ServerInterface {
	private final ServerInterface theServer;
	// The user associated with this proxy 
	private String userId;
	private String userType;

	public ServerProxy(String userId, String userType, ServerInterface theServer) throws RemoteException, SecurityException {
		this.theServer = theServer;
		this.userId = userId;
		this.userType = userType;
	}

	@Override
	public List<Candidate> getCandidates() throws RemoteException {
		return theServer.getCandidates();
	}

	@Override
	public List<Vacancy> getVacancies(boolean open, User user) throws RemoteException {
		return theServer.getVacancies(open, user);
	}

	@Override
	public List<User> getUsers(UserType userType, boolean status) throws RemoteException {
		return theServer.getUsers(userType, status);
	}

	@Override
	public UserType getUserType(String userId) throws RemoteException {
		return theServer.getUserType(userId);
	}

	@Override
	public Vacancy getVacancy(int vacancyId) throws RemoteException {
		return theServer.getVacancy(vacancyId);
	}

	@Override
	public RemoteInputStream getVacancyProfile(String fileName) throws RemoteException {
		return theServer.getVacancyProfile(fileName);
	}

	@Override
	public boolean addVacancyProfile(Vacancy vacancy, RemoteInputStream profileData, String oldFileName) throws RemoteException {
		return theServer.addVacancyProfile(vacancy, profileData, oldFileName);
	}

	@Override
	public boolean removeVacancyProfile(Vacancy vacancy) throws RemoteException {
		return theServer.removeVacancyProfile(vacancy);
	}

	@Override
	public boolean changeVacancyStatus(Vacancy vacancy) throws RemoteException {
		return theServer.changeVacancyStatus(vacancy);
	}

	@Override
	public List<Organisation> getOrganisations() throws RemoteException {
		return theServer.getOrganisations();
	}

	@Override
	public List<Contact> getOrganisationsContacts(Organisation organisation) throws RemoteException {
		return theServer.getOrganisationsContacts(organisation);
	}

	@Override
	public boolean addVacancy(Vacancy vacancy, RemoteInputStream profileData) throws RemoteException {
		return theServer.addVacancy(vacancy, profileData);
	}

	@Override
	public boolean removeVacancy(Vacancy vacancy) throws RemoteException {
		return theServer.removeVacancy(vacancy);
	}

	@Override
	public Organisation getOrganisation(int organisationId) throws RemoteException {
		return theServer.getOrganisation(organisationId);
	}

	@Override
	public RemoteInputStream getOrganisationTob(String fileName) throws RemoteException {
		return theServer.getOrganisationTob(fileName);
	}

	@Override
	public boolean addOrganisationTob(Organisation organisation, RemoteInputStream tobData, String oldFileName) throws RemoteException {
		return theServer.addOrganisationTob(organisation, tobData, oldFileName);
	}

	@Override
	public boolean removeOrganisationTob(Organisation organisation) throws RemoteException {
		return theServer.removeOrganisationTob(organisation);
	}

	@Override
	public boolean addOrganisation(Organisation organisation, RemoteInputStream tobData) throws RemoteException {
		return theServer.addOrganisation(organisation, tobData);
	}

	@Override
	public boolean removeOrganisation(Organisation organisation) throws RemoteException {
		return theServer.removeOrganisation(organisation);
	}

	@Override
	public boolean addCandidate(Candidate candidate, RemoteInputStream cvData) throws RemoteException {
		return theServer.addCandidate(candidate, cvData);
	}
}

package server;

import interfaces.ServerInterface;
import interfaces.UserType;

import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.healthmarketscience.rmiio.RemoteInputStream;

import database.DaoFactory;
import database.beans.Candidate;
import database.beans.CandidateSkill;
import database.beans.Contact;
import database.beans.Event;
import database.beans.Organisation;
import database.beans.Search;
import database.beans.Skill;
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

	@Override
	public RemoteInputStream getOrganisationTob(String fileName) throws RemoteException {
		return DaoFactory.getOrganisationDao().getOrganisationTob(fileName);
	}

	@Override
	public boolean addOrganisationTob(Organisation organisation, RemoteInputStream tobData, String oldFileName) throws RemoteException {
		return DaoFactory.getOrganisationDao().addOrganisationTob(organisation, tobData, oldFileName);
	}

	@Override
	public boolean removeOrganisationTob(Organisation organisation) throws RemoteException {
		return DaoFactory.getOrganisationDao().removeOrganisationTob(organisation);
	}

	@Override
	public boolean addOrganisation(Organisation organisation, RemoteInputStream tobData) throws RemoteException {
		return DaoFactory.getOrganisationDao().addOrganisation(organisation, tobData);
	}

	@Override
	public boolean removeOrganisation(Organisation organisation) throws RemoteException {
		return DaoFactory.getOrganisationDao().removeOrganisation(organisation);
	}

	@Override
	public boolean addCandidate(Candidate candidate, RemoteInputStream cvData) throws RemoteException {
		return DaoFactory.getCandidateDao().addCandidate(candidate, cvData);
	}

	@Override
	public boolean removeCandidate(Candidate candidate) throws RemoteException {
		return DaoFactory.getCandidateDao().removeCandidate(candidate);
	}

	@Override
	public boolean addContact(Contact contact) throws RemoteException {
		return DaoFactory.getContactDao().addContact(contact);
	}

	@Override
	public boolean removeContact(Contact contact) throws RemoteException {
		return DaoFactory.getContactDao().removeContact(contact);
	}

	@Override
	public List<Skill> getSkills() throws RemoteException {
		return DaoFactory.getSkillDao().getSkills();
	}

	@Override
	public List<Candidate> searchCandidates(Search search) throws RemoteException {
		return DaoFactory.getCandidateDao().searchCandidates(search);
	}

	@Override
	public Candidate getCandidate(int candidateId) throws RemoteException {
		return DaoFactory.getCandidateDao().getCandidate(candidateId);
	}

	@Override
	public RemoteInputStream getCandidateCV(String fileName) throws RemoteException {
		return DaoFactory.getCandidateDao().getCandidateCV(fileName);
	}

	@Override
	public boolean addLinkedInProfile(Candidate candidate, URL profileURL) throws RemoteException {
		return DaoFactory.getCandidateDao().addLinkedInProfile(candidate, profileURL);
	}

	@Override
	public boolean removeLinkedInProfile(Candidate candidate) throws RemoteException {
		return DaoFactory.getCandidateDao().removeLinkedInProfile(candidate);
	}

	@Override
	public boolean addCandidateCv(Candidate candidate, RemoteInputStream remoteFileData, String oldFileName) throws RemoteException {
		return DaoFactory.getCandidateDao().addCandidateCv(candidate, remoteFileData, oldFileName);
	}

	@Override
	public boolean removeCandidateCv(Candidate candidate) throws RemoteException {
		return DaoFactory.getCandidateDao().removeCandidateCv(candidate);
	}

	@Override
	public List<Event> getShortlist(int vacancyId) throws RemoteException {
		return DaoFactory.getEventDao().getShortlist(vacancyId);
	}

	@Override
	public boolean addCandidatesToShortlist(List<Candidate> candidates, Vacancy vacancy, String userId) throws RemoteException {
		return DaoFactory.getEventDao().addCandidatesToShortlist(candidates, vacancy, userId);
	}

	@Override
	public boolean removeCandidateFromShortlist(int candidateId, int vacancyId) throws RemoteException {
		return DaoFactory.getEventDao().removeCandidateFromShortlist(candidateId, vacancyId);
	}

	@Override
	public boolean updateCandidateDetails(Candidate candidate) throws RemoteException {
		return DaoFactory.getCandidateDao().updateCandidateDetails(candidate);
	}

	@Override
	public List<CandidateSkill> getCandidateSkills(int candidateId) throws RemoteException {
		return DaoFactory.getCandidateHasSkillDao().getCandidateSkills(candidateId);
	}
	
	@Override
	public boolean addSkillToCandidate(Skill skill, Candidate candidate, String userId) throws RemoteException {
		return DaoFactory.getCandidateHasSkillDao().addSkillToCandidate(skill, candidate, userId);
	}
	
	@Override
	public boolean removeSkillFromCandidate(Skill skill, Candidate candidate) throws RemoteException {
		return DaoFactory.getCandidateHasSkillDao().removeSkillFromCandidate(skill, candidate);
	}

	@Override
	public boolean updateVacancyDetails(Vacancy vacancy) throws RemoteException {
		return DaoFactory.getVacancyDao().updateVacancyDetails(vacancy);
	}

	@Override
	public boolean updateOrganisationDetails(Organisation organisation) throws RemoteException {
		return DaoFactory.getOrganisationDao().updateOrganisationDetails(organisation);
	}

	@Override
	public List<Event> getCandidateEvents(int candidateId) throws RemoteException {
		return DaoFactory.getEventDao().getCandidateEvents(candidateId);
	}

	@Override
	public List<Vacancy> getOrganisationVacancies(int organisationId) throws RemoteException {
		return DaoFactory.getVacancyDao().getOrganisationVacancies(organisationId);
	}

	@Override
	public boolean addEvent(Event event) throws RemoteException {
		return DaoFactory.getEventDao().addEvent(event);
	}

	@Override
	public boolean removeEvent(int eventId) throws RemoteException {
		return DaoFactory.getEventDao().removeEvent(eventId);
	}

	@Override
	public boolean saveCandidateNotes(int candidateId, String notes) throws RemoteException {
		return DaoFactory.getCandidateDao().saveCandidateNotes(candidateId, notes);
	}
}

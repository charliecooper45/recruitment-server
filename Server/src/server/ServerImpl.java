package server;

import interfaces.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import database.DaoFactory;
import database.beans.Candidate;
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
	public List<User> getUsers(String userType, boolean status) throws RemoteException {
		return DaoFactory.getUserDao().getUsers(userType, status);
	}
}

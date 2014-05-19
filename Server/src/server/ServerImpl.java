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
	public List<Candidate> listCandidates() {
		return DaoFactory.getCandidateDao().listCandidates();
	}

	@Override
	public List<Vacancy> listVacancies(boolean open, User user) {
		return DaoFactory.getVacancyDao().listVacancies(open, user);
	}
}

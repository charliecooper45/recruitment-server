package server;

import interfaces.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import database.DaoFactory;
import database.beans.Candidate;

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
}

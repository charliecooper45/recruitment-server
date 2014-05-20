package server;

import interfaces.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import database.beans.Candidate;
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
	public List<User> getUsers(String userType, boolean status) throws RemoteException {
		return theServer.getUsers(userType, status);
	}
}

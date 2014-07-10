package server;

import interfaces.LoginInterface;
import interfaces.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import database.DaoFactory;
import database.UserDao;

public class LoginImpl extends UnicastRemoteObject implements LoginInterface {
	private static final long serialVersionUID = 1L;
	private ServerInterface theServer;
	
	public LoginImpl(ServerInterface theServer) throws RemoteException {
		this.theServer = theServer;
	}

	@Override
	public ServerInterface login(String userId, String password) throws RemoteException, SecurityException {
		//TODO NEXT: set account status and check account type
		UserDao userDao = DaoFactory.getUserDao();
		
		boolean validUser = userDao.checkValidUser(userId);
		
		if(!validUser) 
			throw new SecurityException("Login failed: unknown user.");
		
		String accountType = userDao.checkPassword(userId, password);
		
		if(accountType == null)
			throw new SecurityException("Login failed: password is incorrect.");
		
		return new ServerProxy(userId, accountType, theServer);
	}
}

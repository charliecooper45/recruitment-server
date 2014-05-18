package server;

import interfaces.LoginInterface;
import interfaces.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class LoginImpl extends UnicastRemoteObject implements LoginInterface {
	private ServerInterface theServer;
	
	public LoginImpl(ServerInterface theServer) throws RemoteException {
		this.theServer = theServer;
	}

	@Override
	public ServerInterface login(String userId, String password) throws RemoteException, SecurityException {
		System.out.println("Client attempting to login!");
		
		throw new SecurityException("Unable to login to server, username or password is incorrect");
	}
}

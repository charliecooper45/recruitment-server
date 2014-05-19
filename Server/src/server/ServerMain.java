package server;

import interfaces.LoginInterface;
import interfaces.ServerInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import database.VacancyDao;
import database.beans.User;
import database.beans.Vacancy;

/**
 * Entry point to the server.
 * @author Charlie
 */
public class ServerMain {
	public static void main(String[] args) {
		try {
			ServerInterface theServer = new ServerImpl();
			LoginInterface loginServer = new LoginImpl(theServer);

			Naming.rebind("RecruitmentServer", loginServer);

			System.out.println(new Date() + ": server up and running...");
		} catch (RemoteException | SecurityException | MalformedURLException e) {
			// TODO NEXT: Handle exceptions
			e.printStackTrace();
		}
	}
}

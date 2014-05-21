package server;

import interfaces.LoginInterface;
import interfaces.ServerInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.Naming;
import java.util.Date;

/**
 * Entry point to the server.
 * @author Charlie
 */
public class ServerMain {
	private final static String SERVER_FOLDER;
	public final static String VACANCY_PROFILES_FOLDER;

	static {
		SERVER_FOLDER = "C:/Users/Charlie/Documents/My Dropbox/Open University/TM470 - The computing and IT project/6. Coding/Test Server/Server";
		VACANCY_PROFILES_FOLDER = SERVER_FOLDER + "/Vacancy Profiles";
	}

	public static void main(String[] args) {
		try {
			ServerMain.setupServer();
			ServerInterface theServer = new ServerImpl();
			LoginInterface loginServer = new LoginImpl(theServer);

			Naming.rebind("RecruitmentServer", loginServer);

			System.out.println(new Date() + ": server up and running...");
		} catch (IOException | SecurityException e) {
			// TODO NEXT: Handle exceptions
			e.printStackTrace();
		}
	}

	public static void setupServer() throws IOException {
		// checks if the server folder exists
		Path serverFolderPath = Paths.get(SERVER_FOLDER);
		if (!Files.exists(serverFolderPath)) {
			
			// create the main server directory
			Files.createDirectory(serverFolderPath);
			
			// create the vacancy specs folder directory
			Files.createDirectory(Paths.get(VACANCY_PROFILES_FOLDER));
		} 
	}
}

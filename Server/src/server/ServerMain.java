package server;

import interfaces.LoginInterface;
import interfaces.ServerInterface;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.util.Date;

/**
 * Entry point to the server.
 * @author Charlie
 */
public class ServerMain {
	private final static String SERVER_FOLDER;
	public final static String VACANCY_PROFILES_FOLDER;
	public final static String ORGANISATION_TOB_FOLDER;
	public final static String CANDIDATE_CV_FOLDER;

	static {
		SERVER_FOLDER = "C:/Users/Charlie/Documents/My Dropbox/Open University/TM470 - The computing and IT project/6. Coding/Test Server/Server";
		VACANCY_PROFILES_FOLDER = SERVER_FOLDER + "/Vacancy Profiles";
		ORGANISATION_TOB_FOLDER = SERVER_FOLDER + "/Organisation Terms of business";
		CANDIDATE_CV_FOLDER = SERVER_FOLDER + "/Candidate CVs";
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
			
			// create the organisation TOBs directory
			Files.createDirectory(Paths.get(ORGANISATION_TOB_FOLDER));
		}
	}

	public static void storeFile(InputStream inStream, Path filePath) throws IOException {
		// write the file to a location
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(filePath.toFile());
			int size = 0;
			byte[] byteBuff = new byte[1024];
			while ((size = bufferedInputStream.read(byteBuff)) != -1) {
				outputStream.write(byteBuff, 0, size);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			outputStream.close();
			bufferedInputStream.close();
		}
	}

	public static Path getCorrectFilePath(String path, String fileName) {
		Path filePath = Paths.get(path + "/" + fileName);
		Path newFilePath = null;

		if (Files.exists(filePath)) {
			int i = 1;
			newFilePath = filePath;
			
			// keep incrementing until an acceptable filename is found
			while (true) {
				if (Files.exists(newFilePath)) {
					newFilePath = Paths.get(filePath.toUri());
					int index = fileName.lastIndexOf("."); 
					String newFileName = fileName.substring(0, index) + "(" + i + ")" + fileName.substring(index);
					newFilePath = Paths.get(path + "/" + newFileName);
					i++;
				} else {
					return newFilePath;
				}
			}
		}
		return filePath;
	}
}

package database.beans;

/**
 * Bean that represents an instance of the entity User on the recruitment database. 
 * @author Charlie
 */
public class User {
	private String id;
	private String password;
	private String firstName;
	private String surname;
	private String emailAddress;
	private String phoneNumber;
	private boolean accountStatus;
	private String accountType;
	
	public User(String id, String password, String firstName, String surname, String emailAddress, String phoneNumber, boolean accountStatus, String accountType) {
		this.id = id;
		this.password = password;
		this.firstName = firstName;
		this.surname = surname;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.accountStatus = accountStatus;
		this.accountType = accountType;
	}
}

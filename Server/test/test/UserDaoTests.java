package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import interfaces.UserType;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import database.DaoFactory;
import database.beans.User;

public class UserDaoTests {

	@Before
	public void setup() {
		// called before every test method
		TestUtilities.resetTestDatabase();
		
		User user = new User("AA01", "testtest", "Andrew", "Adlington", "andrew.adlington@gmail.com", "085435 234 234", true, UserType.STANDARD);
		User user2 = new User("BB01", "testtest", "Ben", "Bridges", "ben.bridges@hotmail.com", "075484 011 121", true, UserType.ADMINISTRATOR);
		User user3 = new User("CC01", "testtest", "Charlotte", "Cooper", "cc@hotmail.com", "045124 779 121", false, UserType.STANDARD);
		DaoFactory.getUserDao().addUser(user);
		DaoFactory.getUserDao().addUser(user2);
		DaoFactory.getUserDao().addUser(user3);
	}
	
	@Test
	public void testCorrectLogin() {
		boolean validUser = DaoFactory.getUserDao().checkValidUser("AA01");
		assertTrue("User is not valid", validUser);
		
		String validPassword = DaoFactory.getUserDao().checkPassword("AA01", "testtest");
		assertNotNull("Password is not valid", validPassword);
		assertEquals("Account type is not correct", validPassword, "standard");
	}
	
	@Test
	public void testIncorrectUsername() {
		boolean validUser = DaoFactory.getUserDao().checkValidUser("XX01");
		
		assertFalse("Username is correct", validUser);
	}
	
	@Test
	public void testIncorrectPassword() {
		String correctPassword = DaoFactory.getUserDao().checkPassword("AA01", "test");
		
		assertNull("Password is correct", correctPassword);
	}
	
	@Test
	public void testCreateUser() {
		User user = new User("CA01", "testtest", "Charlotte", "Adams", "charlotte@hotmail.com", "084652 011 121", true, UserType.STANDARD);
		
		boolean userAdded = DaoFactory.getUserDao().addUser(user);
		
		assertTrue("addUser method returned false", userAdded);
		
		User returnedUser = DaoFactory.getUserDao().getUser("CA01");
		assertNotNull("No user found with user id", returnedUser);
	}
	
	@Test
	public void testRemoveUser() {
		User user = new User("BB01", null, null, null, null, null, false, null);
		
		boolean userRemoved = DaoFactory.getUserDao().removeUser(user);
		
		assertTrue("removeUser method returned false", userRemoved);
		
		User returnedUser = DaoFactory.getUserDao().getUser("BB01");
		assertNull("User found with user id", returnedUser);
	}
	
	@Test
	public void testGetUsers() {
		// get all users
		List<User> users = DaoFactory.getUserDao().getUsers(null, false);
		assertEquals("Incorrect number of users", 3, users.size());
		
		// get administrators only
		users = DaoFactory.getUserDao().getUsers(UserType.ADMINISTRATOR, false);
		assertEquals("Incorrect number of users", 1, users.size());
		
		// get standard users only
		users = DaoFactory.getUserDao().getUsers(UserType.STANDARD, false);
		assertEquals("Incorrect number of users", 2, users.size());
		
		// get active users
		users = DaoFactory.getUserDao().getUsers(null, true);
		assertEquals("Incorrect number of users", 2, users.size());
		
		// remove all users then test that none are returned
		TestUtilities.resetTestDatabase();
		users = DaoFactory.getUserDao().getUsers(null, false);
		assertEquals("Incorrect number of users", 0, users.size());
	}
	
	@Test
	public void testGetUser() {
		User user = DaoFactory.getUserDao().getUser("AA01");
		
		assertNotNull("User is null", user);
		assertEquals("User id is incorrect", "AA01", user.getUserId());
	}
	
	@Test
	public void testGetUserType() {
		UserType administrator = DaoFactory.getUserDao().getUserType("BB01");
		assertEquals("User type is not correct", UserType.ADMINISTRATOR, administrator);
		UserType standard = DaoFactory.getUserDao().getUserType("AA01");
		assertEquals("User type is not correct", UserType.STANDARD, standard);
	}
	
	@Test
	public void testUpdateUserDetails() {
		User user = new User("CC01", "test", "Claire", "Smith", "cs@hotmail.com", "045124 789 121", true, UserType.ADMINISTRATOR);
		boolean updated = DaoFactory.getUserDao().updateUserDetails(user);
		assertTrue("updateUserDetails method returned false", updated);
	}
}

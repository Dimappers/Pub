package dimappers.android.server;

import java.util.HashMap;
import java.util.LinkedList;

public class UserManager {
	
	private UserManager() {
		// Private constructor to make class static
	}

	private static HashMap<String, ServerUser> users;
	private static int userCounter;
	private static final int maxUsers = 10000;
	
	public static void init() {
		users = new HashMap<String, ServerUser>();
		userCounter = 0;
	}
	
	public static void addUser(String userId) {
		/* Creates a new user based on the id, if the user is already there, does nothing */
		if (!users.containsKey(userId) && userCounter < maxUsers) {
			ServerUser tmpUser = new ServerUser(userId);
			users.put(userId,  tmpUser);
			++userCounter;
		}
	}
	
	public static void addEvent(String userId, int eventId) {
		/* Adds the eventId to the given user, if no event exists, does nothing
		 * if no user exists, does nothing 
		 * */
		ServerUser user = users.get(userId);
		if (user != null) {
			user.addEvent(eventId);
		}
	}
	
	public static void markForUpdate(String userId, int eventId) {
		/* Flags that the event needs to be refreshed for the user. If userId or eventId doesn't
		 * exist, does nothing
		 */
		ServerUser user = users.get(userId);
		if (user != null) {
			user.setUpdate(eventId, false);
		}
	}
	
	public static LinkedList<Integer> getUpdate(String userId) {
		/* Returns a Linked List of events that need to be refreshed for the given user. If the
		 * user doesn't exist, returns null
		 */
		ServerUser user = users.get(userId);
		if (user != null) {
			return user.getOutOfDateEvents();
		}
		else { return null; }
	}
	
	public static LinkedList<Integer> getFullUpdate(String userId) {
		ServerUser user = users.get(userId);
		if (user != null) {
			return user.getAllEvents();
		}
		else { return null; }
	}
}

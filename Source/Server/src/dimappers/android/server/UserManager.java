package dimappers.android.server;

import java.util.HashMap;
import java.util.LinkedList;

import dimappers.android.PubData.User;

public class UserManager {
	
	private UserManager() {
		// Private constructor to make class static
	}

	private static HashMap<Integer, ServerUser> users;
	private static int userCounter;
	private static final int maxUsers = 10000;
	
	public static void init() {
		users = new HashMap<Integer, ServerUser>();
		userCounter = 0;
	}
	
	public static void addUser(User user) {
		/* Creates a new user based on the id, if the user is already there, does nothing */
		if (!users.containsKey(user.getUserId()) && userCounter < maxUsers) {
			ServerUser tmpUser = new ServerUser(user.getUserId());
			users.put(user.getUserId(),  tmpUser);
			++userCounter;
		}
	}
	
	public static void addEvent(User user, int eventId) {
		/* Adds the eventId to the given user, if no event exists, does nothing
		 * if no user exists, does nothing 
		 * */
		ServerUser sUser = users.get(user.getUserId());
		if (sUser != null) {
			sUser.addEvent(eventId);
		}
	}
	
	public static void markForUpdate(User user, int eventId) {
		/* Flags that the event needs to be refreshed for the user. If userId or eventId doesn't
		 * exist, does nothing
		 */
		ServerUser sUser = users.get(user.getUserId());
		if (sUser != null) {
			sUser.setUpdate(eventId, false);
		}
	}
	
	public static LinkedList<Integer> getUpdate(User user) {
		/* Returns a Linked List of events that need to be refreshed for the given user. If the
		 * user doesn't exist, returns null
		 */
		ServerUser sUser = users.get(user.getUserId());
		if (sUser != null) {
			return sUser.getOutOfDateEvents();
		}
		else { return null; }
	}
	
	public static LinkedList<Integer> getFullUpdate(User user) {
		ServerUser sUser = users.get(user.getUserId());
		if (sUser != null) {
			return sUser.getAllEvents();
		}
		else { return null; }
	}
}

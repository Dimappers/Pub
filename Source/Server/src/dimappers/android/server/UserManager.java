package dimappers.android.server;

import java.util.HashMap;
import java.util.LinkedList;

import dimappers.android.PubData.UpdateType;
import dimappers.android.PubData.User;

public class UserManager {
	
	private UserManager() {
		// Private constructor to make class static
	}

	private static HashMap<Long, ServerUser> users;
	private static int userCounter;
	private static final int maxUsers = 10000;
	
	public static void init() {
		users = new HashMap<Long, ServerUser>();
		userCounter = 0;
	}
	
	public static void addUser(User user) throws ServerException {
		/* Creates a new user based on the id, if the user is already there, does nothing */
		if (!users.containsKey(user.getUserId()) && userCounter < maxUsers) {
			ServerUser tmpUser = new ServerUser(user.getUserId());
			users.put(user.getUserId(),  tmpUser);
			++userCounter;
		}
		else
		{
			if(userCounter >= maxUsers)
			{
				throw new ServerException(ExceptionType.UserManagerMaxUsers);
			}
			//Otherwise user already exists - no problem
		}
	}
	
	public static void addEvent(User user, int eventId) throws ServerException {
		/* Adds the eventId to the given user, if no event exists, does nothing
		 * if no user exists, does nothing 
		 * */
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		ServerUser sUser = users.get(user.getUserId());
		sUser.addEvent(eventId);
	}
	
	public static void markForUpdate(User user, int eventId) throws ServerException {
		/* Flags that the event needs to be refreshed for the user. If userId or eventId doesn't
		 * exist, does nothing
		 */
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = users.get(user.getUserId());
		sUser.NotifyEventUpdated(eventId);
	}
	
	public static void markForConfirmed(User user, int eventId) throws ServerException {
		/*Marks this event for the user as having been confirmed or denied*/
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = users.get(user.getUserId());
		sUser.NotifyEventConfirmed(eventId);
	}
	
	public static void markForUserResponse(User user, int eventId) throws ServerException {
		/*Marks the event as having someone replied */
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = users.get(user.getUserId());
		sUser.NotifyEventConfirmed(eventId);
	}
	
	public static void markAsUpToDate(User user, int eventId) throws ServerException
	{
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = users.get(user.getUserId());
		sUser.NotifyUpdateSent(eventId);
	}
	
	public static void markAllAsUpToDate(User user) throws ServerException
	{
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = users.get(user.getUserId());
		sUser.NotifyUpdateSent();
	}
	
	public static HashMap<Integer, UpdateType> getUpdate(User user) throws ServerException {
		/* Returns a Linked List of events that need to be refreshed for the given user. If the
		 * user doesn't exist, returns null
		 */
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = users.get(user.getUserId());
		return sUser.getOutOfDateEvents();
	}
	
	public static HashMap<Integer, UpdateType> getFullUpdate(User user) throws ServerException {
		if(!users.containsKey(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = users.get(user.getUserId());
		return sUser.getAllEvents();
	}
}

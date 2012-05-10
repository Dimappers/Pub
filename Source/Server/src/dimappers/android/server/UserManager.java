package dimappers.android.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import dimappers.android.PubData.UpdateType;
import dimappers.android.PubData.User;

public class UserManager {
	
	private UserManager() {
		// Private constructor to make class static
	}

	private static int userCounter;
	private static final int maxUsers = 10000;
	
	public static void init() {
		userCounter = 0;
	}
	
	public static void addUser(User user) throws ServerException, SQLException {
		/* Creates a new user based on the id, if the user is already there, does nothing */
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if (!userIds.contains(user.getUserId()) && userCounter < maxUsers) {
			ServerUser tmpUser = new ServerUser(user.getUserId());
			DatabaseManager.addUser(tmpUser);
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
	
	public static void addEvent(User user, int eventId) throws ServerException, SQLException {
		/* Adds the eventId to the given user, if no event exists, does nothing
		 * if no user exists, does nothing ss
		 * */
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		DatabaseManager.addUserToEvent(user.getUserId(), eventId);
	}
	
	public static void markForUpdate(User user, int eventId) throws ServerException, SQLException {
		/* Flags that the event needs to be refreshed for the user. If userId or eventId doesn't
		 * exist, does nothing
		 */
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		sUser.NotifyEventUpdated(eventId);
		DatabaseManager.updateUser(sUser);
	}
	
	public static void markForConfirmed(User user, int eventId) throws ServerException, SQLException {
		/*Marks this event for the user as having been confirmed or denied*/
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		sUser.NotifyEventConfirmed(eventId);
		DatabaseManager.updateUser(sUser);
	}
	
	public static void markForUserResponse(User user, int eventId) throws ServerException, SQLException {
		/*Marks the event as having someone replied */
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		sUser.NotifyPersonResponded(eventId);
		DatabaseManager.updateUser(sUser);
	}
	
	public static void markAsUpToDate(User user, int eventId) throws ServerException, SQLException
	{
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		sUser.NotifyUpdateSent(eventId);
		DatabaseManager.updateUser(sUser);
	}
	
	public static void markAllAsUpToDate(User user) throws ServerException, SQLException
	{
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		sUser.NotifyUpdateSent();
	}
	
	public static Set<Integer> getUpdate(User user) throws ServerException, SQLException {
		/* Returns a Linked List of events that need to be refreshed for the given user. If the
		 * user doesn't exist, throws an exception
		 */
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		return sUser.getOutOfDateEvents();
	}
	
	public static Set<Integer> getFullUpdate(User user) throws ServerException, SQLException {
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		return sUser.getAllEvents().keySet();
	}
	
	public static UpdateType getUpdateType(User user, int eventId) throws ServerException, SQLException
	{
		ArrayList<Integer> userIds = DatabaseManager.getUserIds();
		if(!userIds.contains(user.getUserId()))
		{
			throw new ServerException(ExceptionType.UserManagerNoSuchUser);
		}
		
		ServerUser sUser = DatabaseManager.getUser(user.getUserId());
		return sUser.getUpdateType(eventId);
	}
}

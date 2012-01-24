package dimappers.android.server;

import java.sql.*;
import java.util.LinkedList;

public class DatabaseManager {
	Connection con;
	
	DatabaseManager() throws SQLException {
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerData", "root", "dimap");
	}
	
	public ServerUser getUser(Integer userId) throws SQLException {
		/* Given the User Id, returns a ServerUser class with the User's information in */
		
		ServerUser user = new ServerUser(userId);
		
		PreparedStatement statement = con.prepareStatement("SELECT User.hasApp, UserEvents.eventId " +
															"FROM User, UserEvents " +
															"WHERE User.id = ? AND User.id = UserEvents.userId");
		
		statement.setInt(1, userId);
		ResultSet rs = statement.executeQuery();
		if (true) { // TODO: Check the result set isn't empty
			while(rs.next()) {
				user.SetHasApp(rs.getBoolean("hasApp"));
				user.addEvent(rs.getInt("eventID"));
			}
		}
		
		return user;
	}
	
	public void addUser(ServerUser user) throws SQLException {
		/* Given the ServerUser class, adds it to the database */
		
		PreparedStatement statementUser = con.prepareStatement("INSERT INTO User(id, hasApp) VALUES (?, ?)");
		PreparedStatement statementUserEvents = con.prepareStatement("INSERT INTO UserEvents(userId, eventID) VALUES (?, ?)");
		
		// Add the User Info the the User table
		statementUser.setInt(1, user.getUserId());
		statementUser.setBoolean(2, user.GetHasApp());
		statementUser.execute();
		
		LinkedList<Integer> events = user.getAllEvents();
		// For each Event in the users event list, add it to the database
		while (events.size() != 0) {
			System.out.println("Events Size: " + events.size());
			statementUserEvents.setInt(1, user.getUserId());
			statementUserEvents.setInt(2, events.pop());
			statementUserEvents.execute();
		}
		
		statementUser.close();
		statementUserEvents.close();
	}
	
	public void addUserToEvent(int userId, int eventId) throws SQLException {
		/* Indicates the user is going to the event by adding the userId to the eventId */
		
		PreparedStatement statement = con.prepareStatement("INSERT INTO UserEvents(userId, eventId) VALUES (?, ?)");
		statement.setInt(1, userId);
		statement.setInt(2, eventId);
	}
}

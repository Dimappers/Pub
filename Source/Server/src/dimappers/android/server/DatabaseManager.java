package dimappers.android.server;

import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.naming.event.EventContext;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.UpdateType;
import dimappers.android.PubData.User;

public class DatabaseManager {
	Connection con;
	
	DatabaseManager() throws SQLException {
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ServerData", "root", "dimap");
	}
	
	public ServerUser getUser(Long userId) throws SQLException {
		/* Given the User Id, returns a ServerUser class with the User's information in */
		
		ServerUser user = new ServerUser(userId);
		
		PreparedStatement statement = con.prepareStatement("SELECT User.hasApp, UserEvents.eventId " +
															"FROM User, UserEvents " +
															"WHERE User.id = ? AND User.id = UserEvents.userId");
		
		statement.setLong(1, userId);
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
		statementUser.setLong(1, user.getUserId());
		statementUser.setBoolean(2, user.GetHasApp());
		statementUser.execute();
		
		HashMap<Integer, UpdateType> events = user.getAllEvents();
		// For each Event in the users event list, add it to the database
		//MARK: Not really sure what this does, tried to adapt for my change but may be a load of rubbish
		for(Integer eventId : events.keySet()) {
			System.out.println("Events Size: " + events.size());
			statementUserEvents.setLong(1, user.getUserId());
			statementUserEvents.setInt(2, eventId);
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
	
	public void setHasApp(ServerUser user, boolean hasApp) throws SQLException {
		/* Given the users hasApp status, sets it to the database */
		
		PreparedStatement statement = con.prepareStatement("INSERT INTO User(hasApp) " +
															"VALUES (?)" +
															"WHERE User.id = ?");
		
		statement.setBoolean(1, hasApp);
		statement.setLong(2, user.getUserId());
		statement.execute();
		
		statement.close();
	}
	
	public void removeUser(ServerUser user) throws SQLException {
		// Removes the user
		
		PreparedStatement statement1 = con.prepareStatement("DELETE from User " +
														 	"WHERE User.id = ?");
		PreparedStatement statement2 = con.prepareStatement("DELETE from UserEvents" +
															"WHERE UserEvents.userId = ?");
		
		statement1.setLong(1, user.getUserId());
		statement2.setLong(1, user.getUserId());
		
		statement1.execute();
		statement2.execute();
		
		statement1.close();
		statement2.close();
	}
	
	public void removeUserFromEvent(ServerUser user, PubEvent event) throws SQLException {
		// Removes the user from the event
		
		PreparedStatement statement = con.prepareStatement( "DELETE from UserEvents" +
															"WHERE UserEvents.userId = ? AND UserEvents.eventId = ?");
		
		statement.setLong(1, user.getUserId());
		statement.setInt(2, event.GetEventId());
		
		statement.execute();
		
		statement.close();
	}
	
	public void removeEvent(PubEvent event) throws SQLException {
		// Removes the event
		PreparedStatement statement1 = con.prepareStatement("DELETE from Events " +
			 												"WHERE Event.id = ?");
		PreparedStatement statement2 = con.prepareStatement("DELETE from UserEvents" +
															"WHERE UserEvents.userId = ?");
	}
	
	public void addEvent(PubEvent event) throws SQLException {
		/* Given an event, adds it to the database */
		
		PreparedStatement statementEvent = con.prepareStatement("INSERT INTO Events(eventId, hostId, startTime, pubId) " +
																"VALUES (?, ?, ?, ?)");
		PreparedStatement statementPubLoc = con.prepareStatement("INSERT INTO PubLocation(poskey, name) " +
																 "VALUES (?, ?)");
		PreparedStatement statementUserEvents = con.prepareStatement("INSERT INTO UserEvents(userId, eventId) " +
																	 "VALUES (?, ?)");
		
		// Set the variables for the entry into the Event table
		Date startTime = new Date(event.GetStartTime().getTimeInMillis());
		long pubIdKey = getPubIdKey(event.GetPubLocation());
		statementEvent.setInt(1, event.GetEventId());
		statementEvent.setLong(2, event.GetHost().getUserId());
		statementEvent.setDate(3, startTime);
		statementEvent.setLong(4, pubIdKey);
		statementEvent.execute();
		
		// Set the variables for the entry into the PubLocation table
		statementPubLoc.setLong(1, pubIdKey);
		statementPubLoc.setString(2, event.GetPubLocation().pubName);
		statementPubLoc.execute();
		
		// Set the variables for the UserEvents table
		Set<User> users = event.GetUsers();
		for (User user : users) {
			statementUserEvents.setLong(1, user.getUserId());
			statementUserEvents.setInt(2, event.GetEventId());
			statementUserEvents.execute();
		}
	}
	
	public PubEvent getEvent(int eventId) throws SQLException {
		/* Given an event Id, returns and Event class containing the right information */
		
		PreparedStatement statement = con.prepareStatement("SELECT Events.hostId, Events.startTime, Events.pubId, " +
															"PubLocation.name, UserEvents.userId " +
															"FROM Events, PubLocation, UserEvents " +
															"WHERE Events.eventId = ? AND PubLocation.poskey = Events.pubId AND " +
															"UserEvents.eventId = ?");
		
		statement.setInt(1, eventId);
		statement.setInt(2, eventId);
		
		ResultSet rs = statement.executeQuery();
		rs.next();
		PubLocation loc = getPubLocation(rs.getLong("Events.pubId"));
		loc.pubName = rs.getString("PubLocation.name");
		Date stime = rs.getDate("Events.startTime");
		Calendar startTime = Calendar.getInstance();
		startTime.setTimeInMillis(stime.getTime());
		PubEvent event = new PubEvent(startTime, loc, getUser(rs.getLong("Events.hostId")));
		event.AddUser(getUser(rs.getLong("UserEvents.userId")));
		while (rs.next()) {
			event.AddUser(getUser(rs.getLong("UserEvents.userId")));
		}
		
		return event;
	}

	public void clearTables() throws SQLException {
		Statement sta1 = con.createStatement();
		sta1.execute("DELETE FROM User");
		sta1.execute("DELETE FROM Events");
		sta1.execute("DELETE FROM UserEvents");
		sta1.execute("DELETE FROM PubLocation");
	}
	
	private long getPubIdKey(PubLocation getPubLocation) {
		int lati = Float.floatToIntBits(getPubLocation.latitudeCoordinate);
		int loni = Float.floatToIntBits(getPubLocation.longitudeCoordinate);
		
		long key = lati << 32 | loni;
		return key;
	}
	
	private PubLocation getPubLocation(long pubIdKey) {
		int lati = (int) pubIdKey >> 32;
		int loni = (int) pubIdKey;
		
		PubLocation pubLoc = new PubLocation();
		pubLoc.latitudeCoordinate = Float.intBitsToFloat(lati);
		pubLoc.longitudeCoordinate = Float.intBitsToFloat(loni);
		
		return pubLoc;
	}
}

package dimappers.android.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class DBTest {
	
	public static void main(String[] args) throws SQLException {
		DatabaseManager.initFromScratch();
		
		ServerUser user1 = new ServerUser(1l);
		ServerUser user2 = new ServerUser(2l);
		ServerUser user3 = new ServerUser(3l);
		
		DatabaseManager.clearTables();
		
		for (long i=1; i<=3; ++i) {
			DatabaseManager.addUser(new ServerUser(i));
			DatabaseManager.addUser(new ServerUser(i+3));
			
			ServerUser user = new ServerUser(i);
			PubLocation loc = new PubLocation();
			Calendar cal = Calendar.getInstance();
			
			loc.latitudeCoordinate = 20 + i;
			loc.longitudeCoordinate = 20 + i;
			loc.pubName = "Pub " + i;
			
			cal.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + ((int)Math.pow((-1), i) * 10000*i));
			PubEvent event = new PubEvent(cal, loc, user);
			event.SetEventId((int)i);
			
			for (long j=1; j<=6; ++j) {
				if (j%i==0) {
					event.AddUser(new ServerUser(j));
				}
			}
			
			DatabaseManager.addEvent(event);
			ArrayList<PubEvent> oldEvents = DatabaseManager.getOldEvents();
			System.out.println("Old events: " + oldEvents.toString());
		}
		
		DatabaseManager.removeEvent(2);
		
		//Object[] u1t = user1.getAllEvents().toArray();
		/*for (int i=0; i<u1t.length; ++i) {
			System.out.println(u1t[i].toString());
		}*/
		user2.SetHasApp(true);
		
		
		System.out.println(DatabaseManager.getUser(1l).toString());
		System.out.println(DatabaseManager.getUser(2l).toString());
		System.out.println(DatabaseManager.getUser(3l).toString());
		System.out.println("Lat: " + DatabaseManager.getEvent(1).GetPubLocation().latitudeCoordinate + " Lon: " + 
							DatabaseManager.getEvent(1).GetPubLocation().longitudeCoordinate);
	}

}

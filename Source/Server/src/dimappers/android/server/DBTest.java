package dimappers.android.server;

import java.sql.SQLException;
import java.util.Calendar;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class DBTest {
	
	public static void main(String[] args) throws SQLException {
		DatabaseManager db = new DatabaseManager();
		ServerUser user1 = new ServerUser(1l);
		ServerUser user2 = new ServerUser(2l);
		ServerUser user3 = new ServerUser(3l);
		
		db.clearTables();
		
		for (long i=1; i<=3; ++i) {
			db.addUser(new ServerUser(i));
			db.addUser(new ServerUser(i+3));
			
			ServerUser user = new ServerUser(i);
			PubLocation loc = new PubLocation();
			Calendar cal = Calendar.getInstance();
			
			loc.latitudeCoordinate = 20 + i;
			loc.longitudeCoordinate = 20 + i;
			loc.pubName = "Pub " + i;
			
			cal.setTimeInMillis(Calendar.getInstance().getTimeInMillis()+100*i);
			PubEvent event = new PubEvent(cal, loc, user);
			event.SetEventId((int)i);
			
			for (long j=1; j<=6; ++j) {
				if (j%i==0) {
					event.AddUser(new ServerUser(j));
				}
			}
			
			db.addEvent(event);
		}
		
		Object[] u1t = user1.getAllEvents().toArray();
		for (int i=0; i<u1t.length; ++i) {
			System.out.println(u1t[i].toString());
		}
		user2.SetHasApp(true);
		
		
		System.out.println(db.getUser(1l).toString());
		System.out.println(db.getUser(2l).toString());
		System.out.println(db.getUser(3l).toString());
		System.out.println("Lat: " + db.getEvent(1).GetPubLocation().latitudeCoordinate + " Lon: " + 
							db.getEvent(1).GetPubLocation().longitudeCoordinate);
	}

}

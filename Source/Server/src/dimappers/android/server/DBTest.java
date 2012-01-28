package dimappers.android.server;

import java.sql.SQLException;

public class DBTest {
	
	public static void main(String[] args) throws SQLException {
		DatabaseManager db = new DatabaseManager();
		ServerUser user1 = new ServerUser(1);
		ServerUser user2 = new ServerUser(2);
		ServerUser user3 = new ServerUser(3);
		
		for (int i=1; i<=3; ++i) {
			System.out.println(i);
			user1.addEvent(i);
			user2.addEvent(i+1);
			user3.addEvent(i+2);
		}
		
		Object[] u1t = user1.getAllEvents().toArray();
		for (int i=0; i<u1t.length; ++i) {
			System.out.println(u1t[i].toString());
		}
		user2.SetHasApp(true);
		
		db.addUser(user1);
		db.addUser(user2);
		db.addUser(user3);
		
		
		System.out.println(db.getUser(1).toString());
		System.out.println(db.getUser(2).toString());
		System.out.println(db.getUser(3).toString());
	}

}

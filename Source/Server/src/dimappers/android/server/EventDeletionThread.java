package dimappers.android.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import dimappers.android.PubData.PubEvent;

public class EventDeletionThread extends Thread {

	static final int hoursDelay = 24; 	// The number of hours the thread will wait till it next deletes old events
	
	EventDeletionThread() throws ServerException {
		super();
		try {
			deleteEvents();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void deleteEvents() throws ServerException, SQLException {
		// Every 24 hours, will delete old events from the server
		while (true) {
			// Delete  the events
			Calendar currentTime = Calendar.getInstance();
			ArrayList<PubEvent> oldEvents = EventManager.getOldEvents();
			
			for (int i=0; i<oldEvents.size(); ++i) {
				EventManager.removeEvent(oldEvents.get(i).GetEventId());
			}
			// Sleep for 24 hours
			try {
				sleep(3600000*hoursDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

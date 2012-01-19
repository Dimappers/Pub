package dimappers.android.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import dimappers.android.PubData.User;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubTripState;

public class ServerUser extends dimappers.android.PubData.User 
{
	private boolean hasApp;
	private HashMap<Integer, Boolean> events;
	
	public ServerUser(String facebookUserName) {
		super(facebookUserName);
		hasApp = false;
		events = new HashMap<Integer, Boolean>();
	}
	
	public void addEvent(int eventId) {
		/* Current adds the event to the user list if it isn't already in, does nothing if the event is currently in */
		if (!events.containsKey(eventId)) {
			events.put(eventId, false);
		}
	}
	
	public LinkedList<Integer> getOutOfDateEvents() {
		// Iterates through each event, checking if it needs refreshing
		// (Not sure if the is the best way of doing it)
		LinkedList<Integer> outOfDateEvents = new LinkedList<Integer>();
		Set<Integer> keys = events.keySet();
		Iterator<Integer> iter = keys.iterator();
		while (true) {
			try {
				int key = iter.next();
				if (!events.get(key)) {
					outOfDateEvents.add(key);
				}
			} catch(NoSuchElementException e) {
				return outOfDateEvents;
			}
		}
	}
	
	public void setUpdate(int eventId, boolean update) {
		if (events.containsKey(eventId)) {
			events.put(eventId, update);
		}
	}
	public boolean GetHasApp() {	return hasApp;	}
	public void SetHasApp(boolean hasApp) {	this.hasApp = hasApp;	}
}

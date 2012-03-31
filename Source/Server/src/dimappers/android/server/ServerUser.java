package dimappers.android.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import dimappers.android.PubData.UpdateType;
import dimappers.android.PubData.User;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubTripState;

public class ServerUser extends dimappers.android.PubData.User 
{
	private boolean hasApp;
	private HashMap<Integer, UpdateType> events; //False means needs updating
	
	public ServerUser(Long facebookUserId) {
		super(facebookUserId);
		hasApp = false;
		events = new HashMap<Integer, UpdateType>();
	}
	
	public void addEvent(int eventId) {
		/* Current adds the event to the user list if it isn't already in, does nothing if the event is currently in */
		if (!events.containsKey(eventId)) {
			events.put(eventId, UpdateType.newEvent);
		}
	}
	
	public HashMap<Integer, UpdateType> getOutOfDateEvents() {
		// Iterates through each event, checking if it needs refreshing
		HashMap<Integer, UpdateType> outOfDateEvents = new HashMap<Integer, UpdateType>();
		
		for(Entry<Integer, UpdateType> eventEntry : events.entrySet())
		{
			if(eventEntry.getValue() != UpdateType.noChangeSinceLastUpdate)
			{
				outOfDateEvents.put(eventEntry.getKey(), eventEntry.getValue());
			}
		}
		
		return outOfDateEvents;
	}
	
	public HashMap<Integer, UpdateType> getAllEvents() {
		return events;
	}
	
	/*public void setUpdate(int eventId, boolean update) throws ServerException {
		if (events.containsKey(eventId)) {
			events.put(eventId, update);
		}
		else
		{
			throw new ServerException(ExceptionType.ServerUserNoSuchEvent);
		}
		
	}*/
	
	/*Event has been updated - this user needs to retrieve it when it next asks*/
	public void NotifyEventUpdated(int eventId) throws ServerException
	{
		if (events.containsKey(eventId)) {
			switch(events.get(eventId))
			{
			case confirmed:
				events.put(eventId, UpdateType.confirmedUpdated);
				break;
			case newEvent:
				// Do nothing since a new event that is updated is still a new event
				break;
			case newEventConfirmed:
				// Still do nothing is still a newEvent that has been confirmed
				break;
			case noChangeSinceLastUpdate:
				events.put(eventId, UpdateType.updatedEvent);
				break;
			}
		}
		else
		{
			throw new ServerException(ExceptionType.ServerUserNoSuchEvent);
		}
	}
	
	public void NotifyEventConfirmed(int eventId)
	{
		if(events.containsKey(eventId))
		{
			switch(events.get(eventId))
			{
			case newEvent:
				events.put(eventId, UpdateType.newEventConfirmed);
				break;
			case noChangeSinceLastUpdate:
				events.put(eventId, UpdateType.confirmed);
				break;
			case updatedEvent:
				events.put(eventId, UpdateType.updatedConfirmed);
				break;
			
			}
		}
	}
	
	public void NotifyUpdateSent()
	{
		//Need to set all events to true has have retrieved them all
		for(Entry<Integer, UpdateType> event : events.entrySet())
		{
			event.setValue(UpdateType.noChangeSinceLastUpdate);
		}
	}
	
	public void NotifyUpdateSent(int eventId) throws ServerException
	{
		if(events.containsKey(eventId))
		{
			events.put(eventId, UpdateType.noChangeSinceLastUpdate);
		}
		else
		{
			throw new ServerException(ExceptionType.ServerUserNoSuchEvent);
		}
	}
	public boolean GetHasApp() 				{	return hasApp;	}
	public void SetHasApp(boolean hasApp)	{	this.hasApp = hasApp;	}
	
	public String toString() {
		/* A toString method to aid debugging */
		String str = "Id: " + super.getUserId() + " hasApp: " + hasApp + "\n";
		str += "Events: \n";
		for(Integer event : this.getAllEvents().keySet())
		{
			str += event;
		}
		str += "\n";
		
		return str;
	}
}

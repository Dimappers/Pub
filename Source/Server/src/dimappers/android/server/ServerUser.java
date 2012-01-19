package dimappers.android.server;

import java.util.HashMap;
import java.util.LinkedList;

import dimappers.android.PubData.User;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubTripState;

public class ServerUser extends dimappers.android.PubData.User 
{
	private boolean hasApp;
	private HashMap<Integer, Boolean> eventStatus; //EventId, needsUpdate (ie true, need an update)
	
	public ServerUser(String facebookUserName) {
		super(facebookUserName);
		hasApp = false;
	}
	
	public boolean GetHasApp()
	{
		return hasApp;
	}
	public void SetHasApp(boolean hasApp)
	{
		this.hasApp = hasApp;
	}
	
	public void AddEvent(int eventId)
	{
		eventStatus.put(eventId, true);
	}
	
	public void NotifyEventUpdated(int eventId)
	{
		eventStatus.remove(eventId);
		eventStatus.put(eventId, true);
	}
	
	public void NotifyUpdateSent()
	{
		//Need to set all events to false has have retrived them all
		for(int eventId : eventStatus.keySet())
		{
			eventStatus.remove(eventId);
			eventStatus.put(eventId, false);
		}
	}
	
	public Integer[] GetEventsThatNeedUpdating()
	{
		LinkedList<Integer> eventsThatNeedUpdating = new LinkedList<Integer>();
		
		//nb, an alternative - more memory, faster, is to keep track of events that have been updated
		for(int eventId : eventStatus.keySet())
		{
			if(eventStatus.get(eventId))
			{
				eventsThatNeedUpdating.add(eventId);
			}
		}
		
		Integer[] arrayEvents = new Integer[eventsThatNeedUpdating.size()];
		eventsThatNeedUpdating.toArray(arrayEvents);
		
		return arrayEvents;
	}
}

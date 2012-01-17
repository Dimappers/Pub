package dimappers.android.server;

import java.util.HashMap;

import dimappers.android.PubData.PubEvent;

public class EventManager
{
	private EventManager()
	{
		//Hidden constructor to make class static
	}
	
	private static HashMap<Integer, PubEvent> events;
	private static int eventsCounter;
	private static final int maxEvents = 10000;
	
	public static void InitFromScratch()
	{
		events = new HashMap<Integer, PubEvent>();
		eventsCounter = 0;
	}
	
	public static void InitFromFile(String loadFile)
	{
		throw new UnsupportedOperationException("Loading from file not implemented");
	}
	
	public static int AddNewEvent(PubEvent event)
	{
		//Find an empty event slot 
		while(events.containsKey(eventsCounter))
		{
			eventsCounter = (eventsCounter + 1) % maxEvents;
		}
		
		events.put(eventsCounter, event);
		
		return eventsCounter++; //return the old value and increment
	}
	
	public static PubEvent GetPubEvent(int pubEventId)
	{
		return events.get(pubEventId);
	}
	
	public void UpdateEvent(int pubEventId, PubEvent event)
	{
		events.remove(pubEventId);
		events.put(pubEventId, event);
	}
}

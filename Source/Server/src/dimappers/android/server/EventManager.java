package dimappers.android.server;

import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	
	public static void SaveToFile() 
	{
		throw new UnsupportedOperationException("Saving to file not implemented");
	}
	
	public static int AddNewEvent(PubEvent event) throws ServerException
	{
		//Find an empty event slot 
		int startingCount = eventsCounter;
		while(events.containsKey(eventsCounter))
		{
			eventsCounter = (eventsCounter + 1) % maxEvents;
			if(eventsCounter == startingCount)
			{
				throw new ServerException(ExceptionType.EventManagerNoSpace);
			}
		}
		
		events.put(eventsCounter, event);
		event.SetEventId(eventsCounter);
		
		return eventsCounter++; //return the old value and increment
	}
	
	public static PubEvent GetPubEvent(int pubEventId) throws ServerException
	{
		if(!events.containsKey(pubEventId))
		{
			throw new ServerException(ExceptionType.EventManagerNoSuchEvent);
		}
		
		return events.get(pubEventId);
	}
	
	public void UpdateEvent(int pubEventId, PubEvent event) throws ServerException
	{
		if(!events.containsKey(pubEventId))
		{
			throw new ServerException(ExceptionType.EventManagerNoSuchEvent);
		}
		
		events.put(pubEventId, event);
	}
}

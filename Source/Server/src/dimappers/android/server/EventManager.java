package dimappers.android.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.PubEvent;

public class EventManager
{
	private EventManager()
	{
		//Hidden constructor to make class static
	}
	
	private static int eventsCounter;
	private static final int maxEvents = 10000;
	
	public static void InitFromScratch()
	{
		eventsCounter = 1;
	}
	
	public static void InitFromFile(String loadFile)
	{
		throw new UnsupportedOperationException("Loading from file not implemented");
	}
	
	public static void SaveToFile() 
	{
		throw new UnsupportedOperationException("Saving to file not implemented");
	}
	
	public static int AddNewEvent(PubEvent event) throws ServerException, SQLException
	{
		//Find an empty event slot 
		ArrayList<Integer> eventIds = DatabaseManager.getEventIds();
		int startingCount = eventsCounter;
		while(eventIds.contains(startingCount))
		{
			eventsCounter = (eventsCounter + 1) % maxEvents;
			if(eventsCounter == startingCount)
			{
				throw new ServerException(ExceptionType.EventManagerNoSpace);
			}
		}
		
		event.SetEventId(eventsCounter);
		
		DatabaseManager.addEvent(event);
		
		return eventsCounter++; //return the old value and increment
	}
	
	public static PubEvent GetPubEvent(int pubEventId) throws ServerException, SQLException
	{
		ArrayList<Integer> eventIds = DatabaseManager.getEventIds();
		if(!eventIds.contains(pubEventId))
		{
			throw new ServerException(ExceptionType.EventManagerNoSuchEvent);
		}
		
		return DatabaseManager.getEvent(pubEventId);
	}
	
	public static void UpdateEvent(PubEvent event) throws ServerException, SQLException
	{
		ArrayList<Integer> eventIds = DatabaseManager.getEventIds();
		
		if(!eventIds.contains(event.GetEventId()))
		{
			throw new ServerException(ExceptionType.EventManagerNoSuchEvent);
		}
		

		DatabaseManager.removeEvent(event.GetEventId());
		DatabaseManager.addEvent(event);
	}
	
	public static void removeEvent(int pubEventId) throws ServerException, SQLException {
		ArrayList<Integer> eventIds = DatabaseManager.getEventIds();
		
		if (!eventIds.contains(pubEventId)) {
			throw new ServerException(ExceptionType.EventManagerNoSuchEvent);
		}
		
		DatabaseManager.removeEvent(pubEventId);
	}
	
	public static ArrayList<PubEvent> getOldEvents() throws SQLException
	{ 
		return DatabaseManager.getOldEvents();
		
	}
	public static void ConfirmTrip(int pubEventId, EventStatus status) throws ServerException, SQLException
	{
		ArrayList<Integer> eventIds = DatabaseManager.getEventIds();
		if(!eventIds.contains(pubEventId))
		{
			throw new ServerException(ExceptionType.EventManagerNoSuchEvent);
		}
		
		PubEvent event = DatabaseManager.getEvent(pubEventId);
		event.setCurrentStatus(status);
		UpdateEvent(event);
	}
	
}

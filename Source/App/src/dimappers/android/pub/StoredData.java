package dimappers.android.pub;

import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;

public class StoredData implements Serializable
{	
	private final int HistoryDepth = 15;
	
	private ArrayList<PubEvent> savedEvents; //Events the users has created and saved
	private ArrayList<PubEvent> sentEvents;
	private ArrayList<PubEvent> invitedEvents;
	private PubEvent[] recentHistory; //Stores the events most recently acted upon (ie went ahead);
	
	private int nextSlot;
	
	public StoredData()
	{
		nextSlot = 0;
		recentHistory = new PubEvent[HistoryDepth];
		savedEvents = new ArrayList<PubEvent>();
		sentEvents = new ArrayList<PubEvent>();
		invitedEvents = new ArrayList<PubEvent>();
	}
	
	public ArrayList<PubEvent> GetSavedEvents()
	{
		return savedEvents;
	}
	
	public ArrayList<PubEvent> GetAllEvents()
	{
		ArrayList<PubEvent> allEvents = new ArrayList<PubEvent>();
		allEvents.addAll(savedEvents);
		allEvents.addAll(sentEvents);
		allEvents.addAll(invitedEvents);
		return allEvents;
	}
	
	public void AddNewSavedEvent(PubEvent savedEvent)
	{
		savedEvents.add(savedEvent);
	}
	
	public void AddNewSentEvent(PubEvent sentEvent)
	{
		if(sentEvent.GetEventId() < 0)
		{
			Log.d(Constants.MsgWarning, "This event does not appear to have been sent to the server");
		}
		
		sentEvents.add(sentEvent);
	}
	
	public void AddNewInvitedEvent(PubEvent invitedEvent)
	{
		invitedEvents.add(invitedEvent);
	}
	
	
	public PubEvent[] GetRecentHistory()
	{
		return recentHistory;
	}
	
	public void PushNewEvent(PubEvent newEvent)
	{
		recentHistory[nextSlot] = newEvent;
		nextSlot++;
		nextSlot %= HistoryDepth;
	}
	
	public PubEvent PeekOldestEvent()
	{
		int i;
		for(i = nextSlot - 1; recentHistory[i] == null && i != nextSlot; --i);
		
		return recentHistory[i]; //returns oldest history or null if no history
	}
	
	//Use these methods to get the active stored data
	private static StoredData instance = null;
	
	public static void Init(StoredData loadedStore)
	{
		if(instance != null)
		{
			Log.d(Constants.MsgWarning, "Data store has already been initalised");
			return;
		}
		
		if(loadedStore == null)
		{
			instance = new StoredData();
		}
		else
		{
			instance = loadedStore;
		}
	}
	
	public static StoredData getInstance()
	{
		if(instance == null)
		{
			Log.d(Constants.MsgError, "Data store has not been initalised");
		}
		return instance;
	}
 }

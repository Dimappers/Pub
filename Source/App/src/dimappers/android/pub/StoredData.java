package dimappers.android.pub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.util.Log;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;

public class StoredData implements Serializable
{	
	//private final int HistoryDepth = 15;
	
	private HashMap<Integer, PubEvent> savedEvents; //Events the users has created and saved
	private HashMap<Integer, PubEvent> sentEvents;
	private HashMap<Integer, PubEvent> invitedEvents;
	//private PubEvent[] recentHistory; //Stores the events most recently acted upon (ie went ahead);
	
	//private int nextHistorySlot;
	private int nextSavedEventId;
	private boolean needsSaving;
	//transient private Editor editor;
	
	
	
	public StoredData()
	{
		//nextHistorySlot = 0;
		//recentHistory = new PubEvent[HistoryDepth];
		savedEvents = new HashMap<Integer, PubEvent>();
		sentEvents = new HashMap<Integer, PubEvent>();
		invitedEvents = new HashMap<Integer, PubEvent>();
		
		nextSavedEventId = -2;
		
		needsSaving = false;
	}
	
	public Collection<PubEvent> GetSavedEvents()
	{
		return savedEvents.values();
	}

	public Collection<PubEvent> GetSentEvents()
	{
		return sentEvents.values();
	}
	
	public Collection<PubEvent> GetInvitedEvents()
	{
		return invitedEvents.values();
	}

	public ArrayList<PubEvent> GetAllEvents()
	{
		ArrayList<PubEvent> allEvents = new ArrayList<PubEvent>();
		allEvents.addAll(savedEvents.values());
		allEvents.addAll(sentEvents.values());
		allEvents.addAll(invitedEvents.values());
		return allEvents;
	}
	
	public void AddNewSavedEvent(PubEvent savedEvent)
	{
		if(savedEvent.GetEventId() >= 0)
		{
			Log.d(Constants.MsgWarning, "This event appears to have been saved");
		}
		
		if(savedEvent.GetEventId() == Constants.EventIdNotAssigned)
		{
			// Skip the error event id
			if(nextSavedEventId == Constants.ErrorEventId)
			{
				--nextSavedEventId;
			}

			savedEvent.SetEventId(nextSavedEventId);

			--nextSavedEventId;
			savedEvents.put(savedEvent.GetEventId(), savedEvent);
		}
		else
		{
			savedEvents.put(savedEvent.GetEventId(), savedEvent); //Override the existing event with the new data
		}
		
		SaveData();
	}
	
	public void AddNewSentEvent(PubEvent sentEvent)
	{
		if(sentEvent.GetEventId() < 0)
		{
			Log.d(Constants.MsgWarning, "This event does not appear to have been sent to the server");
		}
		
		sentEvents.put(sentEvent.GetEventId(), sentEvent);
		
		SaveData();
	}
	
	public void AddNewInvitedEvent(PubEvent invitedEvent)
	{
		if(invitedEvent.GetEventId() < 0)
		{
			Log.d(Constants.MsgWarning, "This event doesn't have a valid ID");
		}
		invitedEvents.put(invitedEvent.GetEventId(), invitedEvent);
		SaveData();
	}
	
	public void DeleteSavedEvent(PubEvent event)
	{
		if(event.GetEventId() >= 0)
		{
			Log.d(Constants.MsgWarning, "This does not appear to be a saved event");
		}
		savedEvents.remove(event.GetEventId());
		
		SaveData();
	}
	
	private void SaveData()
	{
		needsSaving = true;
	}
 }

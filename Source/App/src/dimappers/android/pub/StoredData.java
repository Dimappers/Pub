package dimappers.android.pub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.SharedPreferences.Editor;
import android.util.Base64;
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
	transient private Editor editor;
	
	public StoredData(Editor editor)
	{
		nextSlot = 0;
		recentHistory = new PubEvent[HistoryDepth];
		savedEvents = new ArrayList<PubEvent>();
		sentEvents = new ArrayList<PubEvent>();
		invitedEvents = new ArrayList<PubEvent>();
		
		this.editor = editor;
		
		SaveData();
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
		
		SaveData();
	}
	
	public void AddNewSentEvent(PubEvent sentEvent)
	{
		if(sentEvent.GetEventId() < 0)
		{
			Log.d(Constants.MsgWarning, "This event does not appear to have been sent to the server");
		}
		
		sentEvents.add(sentEvent);
		
		SaveData();
	}
	
	public void AddNewInvitedEvent(PubEvent invitedEvent)
	{
		invitedEvents.add(invitedEvent);
		SaveData();
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
		SaveData();
	}
	
	public PubEvent PeekOldestEvent()
	{
		int i;
		for(i = nextSlot - 1; recentHistory[i] == null && i != nextSlot; --i);
		
		return recentHistory[i]; //returns oldest history or null if no history
	}
	
	private void SaveData()
	{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream objectWriter;
		try {
			objectWriter = new ObjectOutputStream(data);
			objectWriter.writeObject(this);
			objectWriter.close();
		} catch (IOException e) {
			Log.d(Constants.MsgError, "Error saving data");
			return;
		}
		editor.putString(Constants.SaveDataName, new String(Base64.encode(data.toByteArray(), Base64.DEFAULT)));
		editor.commit();
	}
	
	//Use these methods to get the active stored data
	private static StoredData instance = null;
	
	public static void Init(StoredData loadedStore, Editor sharedPrefEditor)
	{
		if(instance != null)
		{
			Log.d(Constants.MsgWarning, "Data store has already been initalised");
			return;
		}
		
		if(loadedStore == null)
		{
			instance = new StoredData(sharedPrefEditor);
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

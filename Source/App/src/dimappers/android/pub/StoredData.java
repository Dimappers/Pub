package dimappers.android.pub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.content.Context;
import android.util.Log;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;

@SuppressWarnings("serial")
public class StoredData implements Serializable
{	
	//Names of hash maps
	public static final String loggedInUserTag = "LUser";
	private static final String fbAuthStringTag ="FbAuth";
	private static final String fbExpiryTag = "FbExp";
	public static final String hostSavedTag = "HostSaved";
	public static final String hostSentTag = "HostSent";
	public static final String invitedTag = "Invited";
	public static final String genericStoresTag = "GenericStores";
	private static final String historyStoreTag = "HistoryStore";
	
	public static final String sentEventsStore = "PubEvent";
	
	//private final int HistoryDepth = 15;
	
	private HashMap<Integer, PubEvent> savedEvents; //Events the users has created and saved
	//private HashMap<Integer, PubEvent> sentEvents;
	private HashMap<Integer, PubEvent> invitedEvents;
	//private PubEvent[] recentHistory; //Stores the events most recently acted upon (ie went ahead);
	
	//private int nextHistorySlot;
	private int nextSavedEventId;
	//private boolean needsSaving;
	
	private Dictionary<String, HashMap<?,? extends IXmlable>> dataStores;
	private HistoryStore historyStore;
	
	private AppUser loggedInUser;
	private String authKey;
	private long expiryDate;
	
	public StoredData()
	{
		//nextHistorySlot = 0;
		//recentHistory = new PubEvent[HistoryDepth];
		savedEvents = new HashMap<Integer, PubEvent>();
		//sentEvents = new HashMap<Integer, PubEvent>();
		invitedEvents = new HashMap<Integer, PubEvent>();
		
		nextSavedEventId = -2;
		
		//needsSaving = false;
		
		dataStores = new Hashtable<String, HashMap<?,? extends IXmlable>>();
		historyStore = new HistoryStore();
	
		loggedInUser =null;
	}
	
	public void setActiveUser(AppUser user)
	{
		loggedInUser = user;
	}
	
	public AppUser getActiveUser()
	{
		return loggedInUser;
	}
	
	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public long getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(long expiryDate) {
		this.expiryDate = expiryDate;
	}

	public <K, V  extends IXmlable> HashMap<K, V> GetGenericStore(IDataRequest<K, V> dataRequest)
	{
		if(dataRequest.getStoredDataId() == Constants.NoDictionaryForGenericDataStore)
		{
			return null;
		}
		
		return GetGenericStore(dataRequest.getStoredDataId());
	}
	
	public <K, V extends IXmlable> HashMap<K, V> GetGenericStore(String storeId)
	{
		@SuppressWarnings("unchecked")
		HashMap<K, V> genericStore = (HashMap<K, V>)dataStores.get(storeId);
		if(genericStore == null)
		{
			genericStore = new HashMap<K, V>();
			dataStores.put(storeId, genericStore);
		}
		
		return genericStore;
	}
	
	public Collection<PubEvent> GetSavedEvents()
	{
		return savedEvents.values();
	}

	/*public Collection<PubEvent> GetSentEvents()
	{
		return sentEvents.values();
	}*/
	
	public Collection<PubEvent> GetInvitedEvents()
	{
		return invitedEvents.values();
	}

	public ArrayList<PubEvent> GetAllEvents()
	{
		ArrayList<PubEvent> allEvents = new ArrayList<PubEvent>();
		allEvents.addAll(savedEvents.values());
		//allEvents.addAll(sentEvents.values());
		allEvents.addAll(invitedEvents.values());
		return allEvents;
	}
	
	public void AddNewSavedEvent(PubEvent savedEvent)
	{
		if(savedEvent.GetEventId() >= 0)
		{
			Log.d(Constants.MsgWarning, "This event appears to have been sent");
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
	}
	
	public PubEvent getEvent(int eventId)
	{
		if(eventId < -1)
		{
			return savedEvents.get(eventId);
		}
		else if(eventId >= 0)
		{
			if(invitedEvents.containsKey(eventId))
			{
				return invitedEvents.get(eventId);
			}
			else
			{
				if(((Hashtable<String, HashMap<?, ? extends IXmlable>>) dataStores).containsKey(sentEventsStore))
				{
					if(dataStores.get(sentEventsStore).containsKey(eventId))
					{
						return (PubEvent)dataStores.get(sentEventsStore).get(eventId);
					}
				}
			}
			return null;
		}
		else
		{
			Log.d(Constants.MsgError, "Should not get message with id -1 - this is like the one we're working on");
			return null;
		}
	}
	
	/*public void AddNewSentEvent(PubEvent sentEvent)
	{
		if(sentEvent.GetEventId() < 0)
		{
			Log.d(Constants.MsgWarning, "This event does not appear to have been sent to the server");
		}
		
		sentEvents.put(sentEvent.GetEventId(), sentEvent);
		
	}*/
	
	public void AddNewInvitedEvent(PubEvent invitedEvent)
	{
		if(invitedEvent.GetEventId() < 0)
		{
			Log.d(Constants.MsgWarning, "This event doesn't have a valid ID");
		}
		invitedEvents.put(invitedEvent.GetEventId(), invitedEvent);
	}
	
	public void DeleteSavedEvent(int eventId)
	{
		if(eventId >= 0)
		{
			Log.d(Constants.MsgWarning, "This does not appear to be a saved event");
		}
		savedEvents.remove(eventId);
	}
	
	//This method does not remove from the server, only deletes the data locally
	public void DeleteSentEvent(int eventId)
	{
		if(invitedEvents.containsKey(eventId))
		{
			invitedEvents.remove(eventId);
		}
		else if(dataStores.get(sentEventsStore).containsKey(eventId))
		{
			dataStores.get(sentEventsStore).remove(eventId);
		}
	}
	
	/*public void notifySentEventHasId(int eventId)
	{
		PubEvent event = sentEvents.get(Constants.EventIdBeingSent);
		event.SetEventId(eventId);
		sentEvents.remove(Constants.EventIdBeingSent);
		sentEvents.put(eventId, event);
	}*/
	
	public HistoryStore getHistoryStore()
	{
		return historyStore;
	}
	
	public String save() {
		Document saveDoc = new Document();
		Element root = new Element("PubSaveData");
		
		Element loggedInUserElement = new Element(loggedInUserTag);
		loggedInUserElement.addContent(loggedInUser.writeXml());
		root.addContent(loggedInUserElement);
		
		Element facebookAuthTokenElement = new Element(fbAuthStringTag);
		facebookAuthTokenElement.setText(authKey);
		root.addContent(facebookAuthTokenElement);
		
		Element facebookExpiryElement = new Element(fbExpiryTag);
		facebookExpiryElement.setText(Long.toString(expiryDate));
		root.addContent(facebookExpiryElement);
		
		Element rootSaved = new Element(hostSavedTag);
		for(PubEvent event : savedEvents.values())
		{
			rootSaved.addContent(event.writeXml());
		}
		root.addContent(rootSaved);
		/*
		Enumeration<String> keyIterator = dataStores.keys();
		Element genericStoresElement = new Element(genericStoresTag);
		while(keyIterator.hasMoreElements()) //for each generic dictionary
		{
			String elementId = keyIterator.nextElement();
			Element genericStoreElement = new Element(elementId);
			
			Element keyTypeElement = new Element("KeyType");
			Element dictionaryTypeElement = new Element("DictionaryType");
			Element valueTypeElement = new Element("ValueType");
			
			dictionaryTypeElement.setText(dataStores.get(elementId).getClass().getName());
			genericStoreElement.addContent(dictionaryTypeElement);
			genericStoreElement.addContent(keyTypeElement);
			genericStoreElement.addContent(valueTypeElement);
			
			Class<? extends Object> keyType = null;
			Class<? extends IXmlable> valueType = null;
			
			for(Entry<?, ? extends IXmlable> entry : dataStores.get(elementId).entrySet())
			{
				Element entryElement = new Element("Entry");
				{
					keyType = entry.getKey().getClass();
					valueType = entry.getValue().getClass();
					
					Element keyElement = new Element("Key");
					keyElement.setText(entry.getKey().toString());
					entryElement.addContent(keyElement);
					
					Element valueElement = new Element("Value");
					valueElement.addContent(entry.getValue().writeXml());
					entryElement.addContent(valueElement);
				}
				
				genericStoreElement.addContent(entryElement);
			}
			keyTypeElement.setText(keyType.getName());
			valueTypeElement.setText(valueType.getName());
			
			genericStoresElement.addContent(genericStoreElement);
		}
		root.addContent(genericStoresElement);
		*/
		
		Element historyStoreElement = new Element(historyStoreTag);
		historyStoreElement.addContent(historyStore.writeXml());
		root.addContent(historyStoreElement);
		
		saveDoc.setRootElement(root);
		
		StringWriter writer = new StringWriter();
		XMLOutputter outputter = new XMLOutputter();
		try {
			outputter.output(saveDoc, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d(Constants.MsgInfo, "Save output: " + writer.toString());
		return writer.toString();
	}
	
	public void Load(String loadedXml)
	{
		SAXBuilder builder = new SAXBuilder();
		StringReader reader = new StringReader(loadedXml);
		Document loadedDoc;
		try {
			loadedDoc = builder.build(reader);
		} catch (JDOMException e) {
			Log.d(Constants.MsgError, "Error reading data: " + e.getMessage());
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "Error reading data: " + e.getMessage());
			return;
		}

		Element root = loadedDoc.getRootElement();
		
		loggedInUser = new AppUser(root.getChild(loggedInUserTag).getChild(AppUser.class.getSimpleName()));
		authKey = root.getChildText(fbAuthStringTag);
		expiryDate = Long.parseLong(root.getChildText(fbExpiryTag));
		
		Element hostSavedElement = root.getChild(hostSavedTag);
		@SuppressWarnings("unchecked")
		List<Element> elements = hostSavedElement.getChildren(PubEvent.class.getSimpleName());
		for(Element savedEventElement : elements)
		{
			PubEvent event = new PubEvent(savedEventElement);
			savedEvents.put(event.GetEventId(), event);
		}
 
		//TODO: Switch to serialisation
		
		/*Element genericStoresElement = root.getChild(genericStoresTag);
		
		List<Element> genericStoresElements= genericStoresElement.getChildren();
		for(Element genericStoreElement : genericStoresElements) //iterate over the dictionaries
		{
			//Then for each dictionary
			//First get the type
			Element keyTypeElement = genericStoreElement.getChild("DictionaryType");
			Class<? extends Object> dictionaryType = null;
			Class<? extends Object> keyType = null;
			Class<? extends IXmlable> valueType = null;
			try {
				dictionaryType = Class.forName(keyTypeElement.getText());
				keyType = Class.forName(genericStoreElement.getChildText("KeyType"));
				valueType = (Class<? extends IXmlable>) Class.forName(genericStoreElement.getChildText("ValueType"));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			HashMap newDictionary = new HashMap();
			
			List<Element> entryElements = genericStoreElement.getChildren("Entry");
			for(Element entryElement : entryElements)
			{
				
			}
			
			Log.d(Constants.MsgInfo, "Have loaded dictionary of type: " + newDictionary.getClass().getSimpleName());			
		}*/
		
		Element historyStoreElement = root.getChild(historyStoreTag);
		if(historyStoreElement != null)
		{
			historyStore = new HistoryStore(historyStoreElement.getChild(HistoryStore.class.getSimpleName()));
		}
		else
		{
			historyStore = new HistoryStore();
		}
	}
	
	public static void writeFile(Context context, String fileName, String dataToBeSaved)
	{
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(dataToBeSaved.getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if(fos!=null)
			{
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String readFile(Context context, String fileName)
	{
		String storedDataString = "";
		FileInputStream file = null;

		//check file exists
		String[] files = context.fileList();
		boolean found = false;
		for(int i = 0; i<files.length; i++)
		{
			if(files[i].equals(fileName))
			{
				found = true;
				break;
			}
		}
		if(!found)
			//if the file has not been found (prevents an exception being thrown by openFileInput(..)
		{
			Log.d(Constants.MsgWarning, "The file " + fileName + " has not been found.");
			return "";
		}
		
		try
		{
			file = context.openFileInput (fileName);
			int character = file.read();
			while(character!=-1)
			{
				storedDataString += (char)character;
				character = file.read();
			}

		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				if(file!=null)
				{
					file.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return storedDataString;
	}
 }

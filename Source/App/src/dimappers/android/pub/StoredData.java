package dimappers.android.pub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.util.Log;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;

public class StoredData implements Serializable
{	
	//Names of hash maps
	public static final String hostSavedTag = "HostSaved";
	public static final String hostSentTag = "HostSent";
	public static final String invitedTag = "Invited";
	public static final String genericStoresTag = "GenericStores";
	private static final String historyStoreTag = "HistoryStore";
	//private final int HistoryDepth = 15;
	
	private HashMap<Integer, PubEvent> savedEvents; //Events the users has created and saved
	//private HashMap<Integer, PubEvent> sentEvents;
	private HashMap<Integer, PubEvent> invitedEvents;
	//private PubEvent[] recentHistory; //Stores the events most recently acted upon (ie went ahead);
	
	//private int nextHistorySlot;
	private int nextSavedEventId;
	private boolean needsSaving;
	
	private Dictionary<String, HashMap<?,? extends IXmlable>> dataStores;
	private HistoryStore historyStore;
	
	public StoredData()
	{
		//nextHistorySlot = 0;
		//recentHistory = new PubEvent[HistoryDepth];
		savedEvents = new HashMap<Integer, PubEvent>();
		//sentEvents = new HashMap<Integer, PubEvent>();
		invitedEvents = new HashMap<Integer, PubEvent>();
		
		nextSavedEventId = -2;
		
		needsSaving = false;
		
		dataStores = new Hashtable<String, HashMap<?,? extends IXmlable>>();
		historyStore = new HistoryStore();
		
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
	
	public void DeleteSavedEvent(PubEvent event)
	{
		if(event.GetEventId() >= 0)
		{
			Log.d(Constants.MsgWarning, "This does not appear to be a saved event");
		}
		savedEvents.remove(event.GetEventId());
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
		Element hostSavedElement = root.getChild(hostSavedTag);
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
		historyStore = new HistoryStore(historyStoreElement.getChild(HistoryStore.class.getSimpleName()));
	}
 }

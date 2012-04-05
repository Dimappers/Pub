package dimappers.android.PubData;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Element;

public class RefreshResponse implements IXmlable {
	
	private final String EventTag = "Event";
	
	//private PubEvent[] newEvents;
	private HashMap<PubEvent, UpdateType> newEvents;
	
	public RefreshResponse(HashMap<PubEvent, UpdateType> events)
	{
		newEvents = events;
	}
	
	public RefreshResponse(Element element)
	{
		readXml(element);
	}
	
	public HashMap<PubEvent, UpdateType> getEvents()
	{
		return newEvents;
	}
	
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		for(Entry<PubEvent, UpdateType> eventEntry : newEvents.entrySet())
		{
			Element entryElement = new Element(EventTag); 
			
			entryElement.addContent(eventEntry.getKey().writeXml());
			
			Element updateTypeElement = new Element(UpdateType.class.getSimpleName());
			updateTypeElement.setText(eventEntry.getValue().toString());
			entryElement.addContent(updateTypeElement);
			
			rootElement.addContent(entryElement);
		}
		return rootElement;
	}

	public void readXml(Element element) {
		List<Element> eventElements = element.getChildren(); 
		newEvents = new HashMap<PubEvent, UpdateType>();
		for(Element eventEntryElement : eventElements)
		{
			PubEvent event = new PubEvent(eventEntryElement.getChild(PubEvent.class.getSimpleName()));
			UpdateType updateType = UpdateType.valueOf(eventEntryElement.getChildText(UpdateType.class.getSimpleName()));
			newEvents.put(event, updateType);
		}
	}
}

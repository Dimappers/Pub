package dimappers.android.PubData;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Element;

public class RefreshResponse implements IXmlable {
	
	private final String EventTag = "Event";
	
	//private PubEvent[] newEvents;
	private HashMap<Integer, UpdateType> newEvents;
	
	public RefreshResponse(HashMap<Integer, UpdateType> events)
	{
		newEvents = events;
	}
	
	public RefreshResponse(Element element)
	{
		readXml(element);
	}
	
	public HashMap<Integer, UpdateType> getEvents()
	{
		return newEvents;
	}
	
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		for(Entry<Integer, UpdateType> eventEntry : newEvents.entrySet())
		{
			Element entryElement = new Element(EventTag); 
			
			entryElement.setText(eventEntry.getKey().toString());
			
			Element updateTypeElement = new Element(UpdateType.class.getSimpleName());
			updateTypeElement.setText(eventEntry.getValue().toString());
			entryElement.addContent(updateTypeElement);
			
			rootElement.addContent(entryElement);
		}
		return rootElement;
	}

	public void readXml(Element element) {
		List<Element> eventElements = element.getChildren(); 
		newEvents = new HashMap<Integer, UpdateType>();
		for(Element eventEntryElement : eventElements)
		{
			Integer event = new Integer(eventEntryElement.getText());
			UpdateType updateType = UpdateType.valueOf(eventEntryElement.getChildText(UpdateType.class.getSimpleName()));
			newEvents.put(event, updateType);
		}
	}
}

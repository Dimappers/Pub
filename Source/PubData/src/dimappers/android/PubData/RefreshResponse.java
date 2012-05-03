package dimappers.android.PubData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

public class RefreshResponse implements IXmlable {
	
	private final String EventTag = "Event";
	
	//private PubEvent[] newEvents;
	private Set<Integer> newEvents;
	
	public RefreshResponse(Set<Integer> events)
	{
		newEvents = events;
	}
	
	public RefreshResponse(Element element)
	{
		readXml(element);
	}
	
	public Set<Integer> getEvents()
	{
		return newEvents;
	}
	
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		for(Integer eventEntry : newEvents)
		{
			Element entryElement = new Element(EventTag); 
			
			entryElement.setText(eventEntry.toString());
			
			rootElement.addContent(entryElement);
		}
		return rootElement;
	}

	public void readXml(Element element) {
		List<Element> eventElements = element.getChildren(); 
		newEvents = new HashSet<Integer>();
		for(Element eventEntryElement : eventElements)
		{
			Integer event = new Integer(eventEntryElement.getText());
			newEvents.add(event);
		}
	}
}

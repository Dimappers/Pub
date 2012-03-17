package dimappers.android.PubData;

import java.util.List;

import org.jdom.Element;

public class RefreshResponse implements IXmlable {

	private PubEvent[] newEvents;
	
	public RefreshResponse(PubEvent[] events)
	{
		newEvents = events;
	}
	
	public RefreshResponse(Element element)
	{
		readXml(element);
	}
	
	public PubEvent[] getEvents()
	{
		return newEvents;
	}
	
	@Override
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		for(PubEvent event : newEvents)
		{
			rootElement.addContent(event.writeXml());
		}
		return rootElement;
	}

	@Override
	public void readXml(Element element) {
		List<Element> eventElements = element.getChildren(PubEvent.class.getSimpleName()); 
		newEvents = new PubEvent[eventElements.size()];
		int i = 0;
		for(Element eventElement : eventElements)
		{
			newEvents[i++] = new PubEvent(eventElement);			
		}
	}
}

package dimappers.android.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.jdom.Element;

import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;

public class HistoryStore implements IXmlable {
	
	private static final String averageElementTag = "AverageElement";
	private static final String oldEventsTag = "OldEvents";
	private static final String averagePeopleTag = "AveragePeople";

	private static final int MaxEvents = 30;
	
	Queue<PubEvent> events;
	AveragePubEvent averagePubEvent;
	int averagePeople;
	
	public HistoryStore() 
	{
		events = new ArrayBlockingQueue<PubEvent>(MaxEvents);
		averagePubEvent = null;
		averagePeople = 10;
	}
	
	public HistoryStore(Element element)
	{
		events = new ArrayBlockingQueue<PubEvent>(MaxEvents);
		readXml(element);
	}
	
	public void addEvent(PubEvent event)
	{
		if(events.size() > MaxEvents)
		{
			PubEvent oldestEvent = events.remove();
			averagePubEvent.addEvent(oldestEvent);
		}
		
		events.add(event);
		averagePeople = (event.GetGoingStatusMap().size() + averagePeople) / 2;
	}
	
	public int getAverageNumberOfFriends() {return averagePeople;}
	
	public List<PubEvent> getPubTrips() 
	{
		List<PubEvent> oldEvents = new ArrayList<PubEvent>(events.size());
		for(PubEvent event : events)
		{
			oldEvents.add(event);
		}
		if(averagePubEvent != null)
		{
			oldEvents.add(averagePubEvent);
		}
		
		return oldEvents;
	}

	
	
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		
		rootElement.addContent(
				new Element(averagePeopleTag)
				.setText(new Integer(averagePeople).toString())
				);
		
		Element oldEventsElement = new Element(oldEventsTag);
		for(PubEvent event : events)
		{
			oldEventsElement.addContent(event.writeXml());
		}
		rootElement.addContent(oldEventsElement);
		
		if(averagePubEvent != null)
		{
			Element averageEventElement = new Element(averageElementTag);
			averageEventElement.addContent(averagePubEvent.writeXml());
			rootElement.addContent(averageEventElement);
		}
		
		return rootElement;
	}

	
	
	public void readXml(Element element) {
		Element oldEventsElement = element.getChild(oldEventsTag);
		@SuppressWarnings("unchecked")
		List<Element> oldEventsElements = oldEventsElement.getChildren(PubEvent.class.getSimpleName());
		
		averagePeople = Integer.parseInt(element.getChild(averagePeopleTag).getText());
		
		for(Element oldEventElement : oldEventsElements)
		{
			events.add(new PubEvent(oldEventElement));
		}
		
		if(element.getChild(averageElementTag) != null)
		{
			averagePubEvent = new AveragePubEvent(element.getChild(averageElementTag).getChild(PubEvent.class.getSimpleName()));
		}
	}
}

package dimappers.android.pub;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Element;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;

@SuppressWarnings("serial")
public class AveragePubEvent extends PubEvent {

	private final String historyElementTag = "History";
	private final String entryElementTag = "TallyEntry";
	private final String valueTag = "Value";
	
	private HashMap<PubLocation, Integer> locationTally;
	private int numberOfPeople;
	
	public AveragePubEvent(Calendar startTime, PubLocation pubLocation,
			User host, int numberOfPeople) {
		super(startTime, pubLocation, host);
		locationTally = new HashMap<PubLocation, Integer>();
		locationTally.put(pubLocation, 1);
		this.numberOfPeople = numberOfPeople;
	}
	
	public void addEvent(PubEvent event)
	{
		PubLocation location = event.GetPubLocation();
		int newCount;
		if(!locationTally.containsKey(location))
		{
			locationTally.put(location, 1);
			newCount = 1;
		}
		else
		{
			int currentCount = locationTally.get(location);
			newCount = ++currentCount;
			locationTally.put(location, newCount);
		}
		
		//If a new pub has same or greater tally to existing one then use this as our location
		if(newCount >= locationTally.get(pubLocation))
		{
			pubLocation = location; 
		}
		
		numberOfPeople = (numberOfPeople + event.GetGoingStatusMap().size()) / 2;
	}
	
	public AveragePubEvent(Element element)
	{
		super(element);
		locationTally = new HashMap<PubLocation, Integer>();
		readXml(element);
	}

	
	public Element writeXml()
	{
		Element element = super.writeXml();
		Element historyElement = new Element(historyElementTag);
		
		for(Entry<PubLocation, Integer> locationTallyEntry : locationTally.entrySet())
		{
			Element locationTallyElement = new Element(entryElementTag);
			locationTallyElement.addContent(locationTallyEntry.getKey().writeXml());
			Element tallyCountElement = new Element(valueTag);
			tallyCountElement.setText(Integer.toString(locationTallyEntry.getValue()));
			locationTallyElement.addContent(tallyCountElement);
			historyElement.addContent(locationTallyElement);
		}
		
		element.addContent(historyElement);
		return element;
	}
	
	
	public void readXml(Element element)
	{
		//super.readXml(element);
		@SuppressWarnings("unchecked")
		List<Element> childElements = element.getChild(historyElementTag).getChildren();
		for(Element locationTallyElement : childElements)
		{
			PubLocation location = new PubLocation(locationTallyElement.getChild(PubLocation.class.getSimpleName()));
			Integer count = Integer.parseInt(locationTallyElement.getChild(valueTag).getText());
			
			locationTally.put(location, count);
		}
	}
}

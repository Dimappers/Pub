package dimappers.android.PubData;

import org.jdom.Element;

public class ConfirmMessage implements IXmlable {

	private final String statusTag = "Status";
	private final String eventIdTag = "EventId";
	
	EventStatus status;
	int eventId;
	
	public ConfirmMessage(EventStatus status, int eventId)
	{
		this.status = status;
		this.eventId = eventId;
	}
	
	public ConfirmMessage(Element element)
	{
		readXml(element);
	}
	
	@Override
	public Element writeXml() {
		Element confirmElement = new Element(getClass().getSimpleName());
		
		Element statusElement = new Element(statusTag);
		statusElement.setText(status.toString());
		confirmElement.addContent(statusElement);
		
		Element eventIdElement = new Element(eventIdTag);
		eventIdElement.setText(Integer.toString(eventId));
		confirmElement.addContent(eventIdElement);
		
		return confirmElement;
	}

	@Override
	public void readXml(Element element) {
		status = EventStatus.valueOf(element.getChildText(statusTag));
		eventId = Integer.parseInt(element.getChildText(eventIdTag));
	}

}

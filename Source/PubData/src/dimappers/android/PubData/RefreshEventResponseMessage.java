package dimappers.android.PubData;

import org.jdom.Element;

public class RefreshEventResponseMessage implements IXmlable
{
	private PubEvent event;
	private UpdateType updateType;
	
	public RefreshEventResponseMessage(PubEvent event, UpdateType updateType)
	{
		this.event = event;
		this.updateType = updateType;
	}
	
	public RefreshEventResponseMessage(Element element)
	{
		readXml(element);
	}
	
	public PubEvent getEvent()
	{
		return event;
	}
	
	public UpdateType getUpdateType()
	{
		return updateType;
	}
	
	public Element writeXml()
	{
		Element rootElement = new Element(getClass().getSimpleName());
		
		rootElement.addContent(event.writeXml());
		
		Element updateTypeElement = new Element(UpdateType.class.getSimpleName());
		updateTypeElement.setText(updateType.toString());
		rootElement.addContent(updateTypeElement);
		
		return rootElement;
	}
	public void readXml(Element element)
	{
		event = new PubEvent(element.getChild(PubEvent.class.getSimpleName()));
		updateType = UpdateType.valueOf(element.getChildText(UpdateType.class.getSimpleName()));
	}

}

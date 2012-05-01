package dimappers.android.PubData;

import org.jdom.Element;

public class RefreshEventMessage implements IXmlable {

	int eventId;
	
	public RefreshEventMessage(int id)
	{
		eventId = id;
	}
	
	public RefreshEventMessage(Element element)
	{
		readXml(element);
	}
	
	public int getEventId()
	{
		return eventId;
	}
	
	@Override
	public Element writeXml() {
		Element root = new Element(this.getClass().getSimpleName());
		root.setText(""+eventId);
		return root;
	}

	@Override
	public void readXml(Element element) {
		eventId = Integer.parseInt(element.getText());
	}

}

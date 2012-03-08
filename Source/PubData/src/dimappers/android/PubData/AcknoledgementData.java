package dimappers.android.PubData;

import java.io.Serializable;

import org.jdom.Document;
import org.jdom.Element;

public class AcknoledgementData implements Serializable, IXmlable {
	
	private static final String EventIdTag = "GlobalEventId";
	
	public int globalEventId;
	
	public AcknoledgementData(Element element)
	{
		readXml(element);
	}
	
	public AcknoledgementData(int globalEventId)
	{
		this.globalEventId = globalEventId;
	}
	
	public Element writeXml()
	{
		Element ackData = new Element(getClass().getSimpleName());
		Element globalEventTag = new Element(EventIdTag);
		globalEventTag.addContent(Integer.toString(globalEventId));
		ackData.addContent(globalEventTag);
		return ackData;
	}
	
	public void readXml(Element element)
	{
		globalEventId = Integer.parseInt(element.getChild(EventIdTag).getText());
	}
	
}

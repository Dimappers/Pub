package dimappers.android.pub;

import org.jdom.Element;

import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;

public class PubEventArray implements IXmlable {

	private PubEvent[] pubEvents;
	
	public PubEventArray(PubEvent[] pubEvents)
	{
		this.pubEvents = pubEvents; 
	}
	
	public PubEvent[] getEvents()
	{
		return pubEvents;
	}
	
	public Element writeXml() {
		return new Element("PubEventArray");
	}

	public void readXml(Element element) {
		//Do nothing since we won't be saving these arrays - they are used for DataRequestRefresh
	}

}

package dimappers.android.pub;

import java.util.HashMap;

import org.jdom.Element;

import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.UpdateType;

public class PubEventArray implements IXmlable {

	private HashMap<PubEvent, UpdateType> pubEvents;
	
	public PubEventArray(HashMap<PubEvent, UpdateType> pubEvents)
	{
		this.pubEvents = pubEvents; 
	}
	
	public HashMap<PubEvent, UpdateType> getEvents()
	{
		return pubEvents;
	}
	
	public Element writeXml() {
		Log.d(Constants.MsgWarning, "SHOULD NOT USE THIS METHOD");
		return new Element("PubEventArray");
	}

	public void readXml(Element element) {
		//Do nothing since we won't be saving these arrays - they are used for DataRequestRefresh
		Log.d(Constants.MsgWarning, "SHOULD NOT USE THIS METHOD");
	}

}

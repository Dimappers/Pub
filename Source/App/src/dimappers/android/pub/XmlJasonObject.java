package dimappers.android.pub;

import java.util.Calendar;

import org.jdom.Element;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;

public class XmlJasonObject extends JSONObject implements IXmlable {

	public static final String jsonTag = "JSON";
	public static final String calendarTag = "Calendar";
	
	Calendar lastUpdated;
	public XmlJasonObject(Element e) throws JSONException
	{
		super(e.getChildText(jsonTag));
		lastUpdated = Calendar.getInstance();
		lastUpdated.setTimeInMillis(Long.parseLong(e.getChildText(calendarTag)));
	}
	
	public XmlJasonObject(String request) throws JSONException {
		super(request);
		lastUpdated = Calendar.getInstance();
	}
	
	
	public Element writeXml() {
		Element e = new Element(getClass().getSimpleName());
		
		Element jsonElement = new Element(jsonTag);
		jsonElement.setText(super.toString());
		e.addContent(jsonElement);
		
		Element calendarElement = new Element(calendarTag);
		calendarElement.setText(Long.toString(lastUpdated.getTimeInMillis()));
		e.addContent(calendarElement);
		
		return e;
	}

	
	public void readXml(Element element) {
		Log.d(Constants.MsgError, "Don't use me!");
	}
	
	public boolean isOutOfDate()
	{
		Calendar weekAfterUpdate = lastUpdated;
		weekAfterUpdate.add(Calendar.DAY_OF_MONTH, Constants.XmlObjectOutOfDateTime);
		return weekAfterUpdate.compareTo(Calendar.getInstance()) < 0;
		
	}
}

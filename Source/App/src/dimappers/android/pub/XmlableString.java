package dimappers.android.pub;

import java.util.Calendar;
import java.util.Date;

import org.jdom.Element;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;

public class XmlableString implements IXmlable {
	
	String contents;
	final static String contentstag = "contents";
	Calendar outOfDate;
	final static String outOfDateTag = "outOfDate";
	
	XmlableString(String contents) 
	{
		this.contents = contents; 
		Calendar current = Calendar.getInstance();
		current.add(Calendar.DATE,Constants.CurrentLocationOutOfDateTime);
		outOfDate = current;
	}
	XmlableString(Element element) {readXml(element);}
	
	public String getContents() {return contents;}
	
	
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		rootElement.addContent(new Element(contentstag).setText(contents));
		rootElement.addContent(new Element(outOfDateTag).setText(new Long(outOfDate.getTimeInMillis()).toString()));
		return rootElement;
	}

	
	public void readXml(Element element) {
		contents = element.getChildText(contentstag);
		outOfDate = Calendar.getInstance();
		outOfDate.setTime(new Date(Long.parseLong(element.getChildText(outOfDateTag))));
	}
	public boolean outOfDate()
	{
		return Calendar.getInstance().after(outOfDate);
	}
}

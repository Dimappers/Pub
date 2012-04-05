package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jdom.Element;

import com.google.api.client.util.Key;

import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;
 
public class PlacesList implements IXmlable{
 
 @Key
 public String status;
 private static final String statuskey = "status";
 
 @Key
 public List<Place> results;
 private static final String resultskey = "results";
 
 static final String outOfDateTag = "outOfDate";
 Calendar outOfDate;

 public void setOutOfDate(Calendar newTime) {outOfDate = newTime;}
 
 public boolean isOutOfDate() {
	 Calendar currentTime = Calendar.getInstance();
	 return (currentTime.compareTo(outOfDate)>=0);
 }
 
public Element writeXml() {
	Element rootElement = new Element(getClass().getSimpleName());
	
	rootElement.addContent(
			new Element(statuskey)
			.setText(status)
			);
	
	Element placesElement = new Element(resultskey);
	for(Place result : results)
	{
		placesElement.addContent(result.writeXml());
	}
	rootElement.addContent(placesElement);

	rootElement.addContent(new Element(outOfDateTag).setText(new Long(outOfDate.getTimeInMillis()).toString()));
	
	return rootElement;
}

public void readXml(Element element) {
	status = element.getChildText(statuskey);
	
	List<Element> resultElements = element.getChildren(resultskey);
	results = new ArrayList<Place>();
	for(Element resultElement : resultElements)
	{
		results.add(new Place(resultElement));
	}
	
	outOfDate = Calendar.getInstance();
	outOfDate.setTime(new Date(Long.parseLong(element.getChildText(outOfDateTag))));
	
}
 
}
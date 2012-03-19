package dimappers.android.pub;

import java.util.ArrayList;
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
}
 
}
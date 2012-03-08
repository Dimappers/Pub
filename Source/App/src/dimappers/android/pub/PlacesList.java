package dimappers.android.pub;

import java.util.List;

import org.jdom.Element;

import com.google.api.client.util.Key;

import dimappers.android.PubData.IXmlable;
 
public class PlacesList implements IXmlable{
 
 @Key
 public String status;
 
 @Key
 public List<Place> results;

public Element writeXml() {
	// TODO Auto-generated method stub
	return null;
}

public void readXml(Element element) {
	// TODO Auto-generated method stub
	
}
 
}
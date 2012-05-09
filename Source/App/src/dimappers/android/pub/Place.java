package dimappers.android.pub;

import org.jdom.Element;

import com.google.api.client.util.Key;

import dimappers.android.PubData.IXmlable;

public class Place implements IXmlable {
 
 @Key
 public String id;
 private static final String idkey = "id";
 
 @Key
 public String name;
 private static final String namekey = "name";
  
 @Key
 public String reference;
 private static final String refkey = "reference";
 
 @Key
 public Geometry geometry;
 private static final String geomkey = "geometry";
 private static final String lockey = "googlelocation";
 private static final String latkey = "latitude"; 
 private static final String longkey = "longitude";		 
 
 @Key
 public String icon;
 private static final String iconkey = "icon";
 
 @Key
 public String vicinity;
 private static final String vicinitykey = "vicinity";
 
 public Place(Element resultElement) {
	readXml(resultElement);
}
 
 public Place() {}

@Override
 public String toString() {
  return name;
 }
 
 @Override
 public boolean equals(Object place2)
 {
	 if(place2 instanceof Place)
	 {
		 return place2.hashCode() == hashCode();
	 }
	 else return false;
 }
 
 @Override
 public int hashCode()
 {
	 return (new Double(Math.pow(2.0, geometry.location.lat)*Math.pow(3.0, geometry.location.lng))).hashCode();
 }

@Override
public Element writeXml() {
	Element rootElement = new Element(getClass().getSimpleName());
	rootElement.addContent(new Element(idkey).setText(id));
	rootElement.addContent(new Element(namekey).setText(name));
	rootElement.addContent(new Element(refkey).setText(reference));
	
	Element geomElement = new Element(geomkey);
	Element locElement = new Element(lockey);
	locElement.addContent(new Element(latkey).setText(new Double(geometry.location.lat).toString()));
	locElement.addContent(new Element(longkey).setText(new Double(geometry.location.lng).toString()));
	geomElement.addContent(locElement);
	rootElement.addContent(geomElement);
	
	rootElement.addContent(new Element(iconkey).setText(icon));
	rootElement.addContent(new Element(vicinitykey).setText(vicinity));
	
	return rootElement;
}

@Override
public void readXml(Element element) {
	id = element.getChildText(idkey);
	name = element.getChildText(namekey);
	reference = element.getChildText(refkey);
	
	geometry = new Geometry();
	geometry.location = new LocationGoogle();
	geometry.location.lat = Double.parseDouble(element.getChild(geomkey).getChild(lockey).getChildText(latkey));
	geometry.location.lng = Double.parseDouble(element.getChild(geomkey).getChild(lockey).getChildText(longkey));
	
	icon = element.getChildText(iconkey);
	vicinity = element.getChildText(vicinitykey);
}
  
}
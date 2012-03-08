package dimappers.android.pub;

import org.jdom.Element;

import com.google.api.client.util.Key;

import dimappers.android.PubData.IXmlable;

public class Place implements IXmlable {
 
 @Key
 public String id;
 
 @Key
 public String name;
  
 @Key
 public String reference;
 
 @Key
 public Geometry geometry;
 
 @Key
 public String icon;
 
 @Key
 public String vicinity;
 
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

public Element writeXml() {
	// TODO Auto-generated method stub
	return null;
}

public void readXml(Element element) {
	// TODO Auto-generated method stub
	
}
  
}
package dimappers.android.PubData;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.jdom.Element;

/* This class holds information about a pub location
 * It does nothing with this data, it is purely a data store
 * 
 * Author: TK */
 
public class PubLocation implements Serializable, IXmlable
{
	//Xml tags
	private static final String latTag = "Latitude";
	private static final String longTag = "Longitude";
	private static final String nameTag = "PubName";
	
	//Properties
	public float 			latitudeCoordinate;
	public float 			longitudeCoordinate;
	public String			pubName;
	private int				rank;

	public PubLocation()
	{
		latitudeCoordinate = 0.0f;
		longitudeCoordinate = 0.0f;
		pubName = "Undefined location";
	}
	
	public PubLocation(Element element)
	{
		readXml(element);
	}
	
	public PubLocation(float latitudeCoordinate, float longitudeCoordinate, String pubName)
	{
		this.latitudeCoordinate = latitudeCoordinate;
		this.longitudeCoordinate = longitudeCoordinate;
		this.pubName = pubName;
	}
	
	@Override
	public String toString()
	{
		return pubName + ": (" + latitudeCoordinate + ", " + longitudeCoordinate +")";
	}
	
	public String getName()
	{
		return pubName;
	}
	
	public void setRank(int rank) {this.rank = rank;}
	public int getRank() {return rank;}
	
	public boolean equals(PubLocation other)
	{
		return other.latitudeCoordinate == latitudeCoordinate && other.longitudeCoordinate == longitudeCoordinate;
	}
	
	public Element writeXml()
	{
		Element pubLocationTag = new Element(getClass().getSimpleName());
		
		Element latElement = new Element(latTag);
		latElement.addContent(Float.toString(latitudeCoordinate));
		Element longElement = new Element(longTag);
		longElement.addContent(Float.toString(longitudeCoordinate));
		Element nameElement = new Element(nameTag);
		nameElement.addContent(pubName);
		
		pubLocationTag.addContent(latElement);
		pubLocationTag.addContent(longElement);
		pubLocationTag.addContent(nameElement);
		
		return pubLocationTag;
	}
	
	public void readXml(Element element)
	{
		latitudeCoordinate = Float.parseFloat(element.getChildText(latTag));
		longitudeCoordinate= Float.parseFloat(element.getChildText(longTag));
		pubName = element.getChildText(nameTag);
	}
	
	@Override
	public int hashCode()
	{
		return new Double(Math.pow(2.0, get2DP(latitudeCoordinate))*Math.pow(3.0, get2DP(longitudeCoordinate))*Math.pow(5.0, pubName.hashCode())).hashCode();
	}
	
	private static double get2DP(double value) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(value));
	}
}


package dimappers.android.pub;

import org.jdom.Element;

import dimappers.android.PubData.IXmlable;

public class XmlableDoubleArray implements IXmlable {

	double[] array;
	
	XmlableDoubleArray(double[] array)
	{
		this.array = array;
	}
	XmlableDoubleArray(Element element)
	{
		readXml(element);
	}
	
	
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		
		Element locationElement = new Element("location");
		locationElement.addContent(new Element("latitude").setText(""+array[0]));
		locationElement.addContent(new Element("longitude").setText(""+array[1]));
		
		rootElement.addContent(locationElement);
		
		return rootElement;
	}

	
	public void readXml(Element element) {
		Element locationElement = element.getChild("location");
		
		double lat = Double.parseDouble(locationElement.getChildText("latitude"));
		double lng = Double.parseDouble(locationElement.getChildText("longitude"));
		
		array = new double[2];
		array[0] = lat;
		array[1] = lng;
	}
	public double[] getArray() {
		return array;
	}

}

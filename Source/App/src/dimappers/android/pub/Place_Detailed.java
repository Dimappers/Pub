package dimappers.android.pub;

import org.jdom.Element;

import com.google.api.client.util.Key;

import dimappers.android.PubData.IXmlable;

public class Place_Detailed extends Place implements IXmlable{

	/* Not implemented information, but available from JSON:
	 * "types": array of Strings
	 * "address_components": array of Objects containing "long_name", "short_name" and "types" for each part of address
	 */
	
	public Place_Detailed(Element resultElement) {
		super(resultElement);
		readXmlShort(resultElement);
	}
	
	public Place_Detailed() {}

	@Key
	public String formatted_phone_number;
	private final static String phonekey = "formattedphonenumber";
	
	@Key
	public String formatted_address;
	private final static String addrkey = "formattedaddress";
	
	@Key
	public double rating;
	private final static String ratingkey = "rating";
	
	@Key
	public String url;
	private final static String urlkey = "url";
	
	@Override
	public Element writeXml()
	{
		Element rootElement = super.writeXml();
		rootElement.addContent(new Element(phonekey).setText(formatted_phone_number));
		rootElement.addContent(new Element(addrkey).setText(formatted_address));
		rootElement.addContent(new Element(ratingkey).setText(new Double(rating).toString()));
		rootElement.addContent(new Element(urlkey).setText(url));
		return rootElement;
	}
	@Override
	public void readXml(Element element)
	{
		super.readXml(element);
		readXmlShort(element);
	}
	
	public void readXmlShort(Element element)
	{
		formatted_phone_number = element.getChildText(phonekey);
		formatted_address = element.getChildText(addrkey);
		rating = Double.parseDouble((element.getChildText(ratingkey)));
		url = element.getChildText(urlkey);
	}
}
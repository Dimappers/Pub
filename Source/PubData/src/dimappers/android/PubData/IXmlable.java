package dimappers.android.PubData;

import org.jdom.Element;

public interface IXmlable {
	public Element writeXml();
	public void readXml(Element element);
}

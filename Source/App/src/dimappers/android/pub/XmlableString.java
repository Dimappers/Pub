package dimappers.android.pub;

import org.jdom.Element;

import dimappers.android.PubData.IXmlable;

public class XmlableString implements IXmlable {
	
	String contents;
	
	XmlableString(String contents) {this.contents = contents;}
	
	public String getContents() {return contents;}
	
	public Element writeXml() {
		// TODO Auto-generated method stub
		return null;
	}

	public void readXml(Element element) {
		// TODO Auto-generated method stub

	}

}

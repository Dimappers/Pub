package dimappers.android.pub;

import org.jdom.Element;

import dimappers.android.PubData.IXmlable;

public class XmlableString implements IXmlable {
	
	String contents;
	
	XmlableString(String contents) {this.contents = contents;}
	XmlableString(Element element) {readXml(element);}
	
	public String getContents() {return contents;}
	
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		rootElement.setText(contents);
		return rootElement;
	}

	public void readXml(Element element) {
		contents = element.getText();
	}

}

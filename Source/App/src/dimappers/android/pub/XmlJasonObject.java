package dimappers.android.pub;

import org.jdom.Element;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;

public class XmlJasonObject extends JSONObject implements IXmlable {

	public XmlJasonObject(Element e) throws JSONException
	{
		super(e.getText());
	}
	@Override
	public Element writeXml() {
		Element e = new Element(getClass().getSimpleName());
		e.setText(super.toString());
		return e;
	}

	@Override
	public void readXml(Element element) {
		// TODO Auto-generated method stub
		Log.d(Constants.MsgError, "Don't use me!");
	}

}

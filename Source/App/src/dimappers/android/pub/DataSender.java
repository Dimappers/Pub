package dimappers.android.pub;

import java.io.IOException;
import java.util.Calendar;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;

public class DataSender {
	
	DataSender() {}
	
	public void sendEvent(PubEvent event) throws IOException {
		
		Element root = new Element("PubEvent");
		Document xmlDoc = new Document(root);
		
		root.addContent(event.GetHost().writeXml());
		root.addContent(new AcknoledgementData(event.GetEventId()).writeXml());
		root.addContent(event.GetPubLocation().writeXml());
		
		root.addContent(event.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.output(xmlDoc, System.out);
		
	}
	
	public void requestUpdate(User u) throws IOException { updates(u,false); }
	
	public void requestAllUpdates(User u) throws IOException { updates(u,true); }
	
	private void updates(User u, boolean full) throws IOException {
	
		Element root = new Element("RequestUpdate");
		Document xmlDoc = new Document(root);
		
		RefreshData rd = new RefreshData(u, full);
		root.addContent(rd.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.output(xmlDoc, System.out);
		
	}
	
	public void sendResponse(User u, int event, boolean going, Calendar freefrom, String msg) throws IOException {
	
		Element root = new Element("RequestUpdate");
		Document xmlDoc = new Document(root);

		ResponseData response = new ResponseData(u,event,going,freefrom,msg);
		root.addContent(response.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.output(xmlDoc, System.out);
		
	}
	
	public void sendEventUpdates(int event, Calendar start, PubLocation pub) throws IOException {
		
		Element root = new Element("UpdateEvent");
		Document xmlDoc = new Document(root);
		
		UpdateData ud = new UpdateData(event, start, pub);
		root.addContent(ud.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.output(xmlDoc, System.out);
		
	}
}

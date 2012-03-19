package dimappers.android.pub;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.RefreshResponse;

public class DataRequestRefresh implements IDataRequest<Long, PubEventArray> {

	private IPubService service;
	private boolean fullRefresh;
	
	public DataRequestRefresh(boolean fullRefresh)
	{
		this.fullRefresh = fullRefresh;
	}
	
	@Override
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	@Override
	public void performRequest(IRequestListener<PubEventArray> listener, HashMap<Long, PubEventArray> storedData) {
		Log.d(Constants.MsgInfo, "Running refresh");
		
		Document xmlRequest = new Document();
		Element root = new Element("Message");
		xmlRequest.setRootElement(root);
		
		Element messageTypeElement = new Element("MessageType");
		messageTypeElement.addContent(MessageType.refreshMessage.toString());
		root.addContent(messageTypeElement);
		
		RefreshData refreshMessage = new RefreshData(service.GetActiveUser(), fullRefresh);
		root.addContent(refreshMessage.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		try {
			outputter.output(xmlRequest, System.out);
		} catch (IOException e) {
			listener.onRequestFail(e);
			return;
		}
		//This creates a test event every 15 min
		/*PubEvent madeUpEvent = new PubEvent(Calendar.getInstance(), service.GetActiveUser());
		madeUpEvent.SetEventId(1288);
		madeUpEvent.SetPubLocation(new PubLocation(0.0f, 0.0f, "Spoons"));
		listener.onRequestComplete(new PubEventArray(new PubEvent[]{madeUpEvent}));*/
		listener.onRequestComplete(new PubEventArray(new PubEvent[]{}));
		return;
		//TODO: Replace with input stream 
		/*SAXBuilder xmlBuilder = new SAXBuilder(); 
		Document returnDocument;
		try
		{
			returnDocument = xmlBuilder.build(System.in);
		} catch (Exception e)
		{
			listener.onRequestFail(e);
			return;
		}
		
		RefreshResponse response = new RefreshResponse(returnDocument.getRootElement());
		listener.onRequestComplete(new PubEventArray(response.getEvents()));*/				
	}

	@Override
	public String getStoredDataId() {
		return Constants.NoDictionaryForGenericDataStore;
	}

}

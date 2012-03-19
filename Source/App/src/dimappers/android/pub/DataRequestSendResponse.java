package dimappers.android.pub;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.media.AudioRecord.OnRecordPositionUpdateListener;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshResponse;
import dimappers.android.PubData.ResponseData;

public class DataRequestSendResponse implements IDataRequest<Long, PubEvent> {

	private IPubService service;
	private boolean isGoing;
	private Calendar freeFromWhen;
	private String msgToHost;
	private int eventId;
	
	public DataRequestSendResponse(boolean isGoing, int eventId, Calendar freeFromWhen, String msgToHost)
	{
		this.isGoing = isGoing;
		this.eventId = eventId;
		this.freeFromWhen = freeFromWhen;
		this.msgToHost = msgToHost;
	}
	
	public DataRequestSendResponse(boolean isGoing, int eventId)
	{
		this(isGoing, eventId, null, "");
	}
	
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	
	public void performRequest(IRequestListener<PubEvent> listener,
			HashMap<Long, PubEvent> storedData) {
		ResponseData rData = new ResponseData(service.GetActiveUser(), eventId, isGoing, freeFromWhen, msgToHost);
		
		Element root = new Element("Message");
		Document xmlDoc = new Document();
		xmlDoc.setRootElement(root);
		
		Element messageTypeElement = new Element("MessageType");
		messageTypeElement.setText(MessageType.respondMessage.toString());
		
		root.addContent(rData.writeXml());
		
		//TODO: Actually send to the server
		XMLOutputter outputter = new XMLOutputter();
		try {
			outputter.output(xmlDoc, System.out);
		} catch (IOException e) {
			listener.onRequestFail(e);
			return;
		}
		
		
		listener.onRequestComplete(null);
		return;
		
		//TODO: Possibly get server to send back latest details
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
		if(response.getEvents().length > 0)
		{
			service.NewEventsRecieved(response.getEvents());
		}
		//Maybe return the latest event if an update?
		listener.onRequestComplete(null);		*/
	}

	
	public String getStoredDataId() {
		return Constants.NoDictionaryForGenericDataStore;
	}

}


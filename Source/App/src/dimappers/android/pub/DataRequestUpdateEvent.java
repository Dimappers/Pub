package dimappers.android.pub;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.UpdateData;

public class DataRequestUpdateEvent implements IDataRequest<Long, PubEvent> {

	private IPubService service;	
	private UpdateData data;
	
	public DataRequestUpdateEvent(PubEvent updatedEvent)
	{
		data = new UpdateData(updatedEvent.GetEventId(), updatedEvent.GetStartTime(), updatedEvent.GetPubLocation());
	}
	
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	
	public void performRequest(IRequestListener<PubEvent> listener,
			HashMap<Long, PubEvent> storedData) {
		Document xmlDoc = new Document();
		Element root= new Element("Message");
		
		Element messageTypeElement = new Element("MessageType");
		messageTypeElement.addContent(MessageType.updateMessage.toString());
		root.addContent(messageTypeElement);
	
		root.addContent(data.writeXml());
		
		//TODO: Put port in stream here
		
		XMLOutputter outputter = new XMLOutputter();
		try {
			outputter.output(xmlDoc, System.out);
		} catch (IOException e) {
			listener.onRequestFail(e);
			return;
		}
		
		listener.onRequestComplete(null); //TODO: Should get latest info about specific event and pass back
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

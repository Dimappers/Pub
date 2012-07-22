package dimappers.android.pub;

import java.util.HashMap;
import org.jdom.Document;
import org.jdom.Element;
import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.RefreshEventMessage;
import dimappers.android.PubData.RefreshEventResponseMessage;
import dimappers.android.PubData.RefreshResponse;
import dimappers.android.PubData.UpdateType;

public class DataRequestRefresh implements IDataRequest<Long, PubEventArray> {

	private IPubService service;
	private boolean fullRefresh;
	
	public DataRequestRefresh(boolean fullRefresh)
	{
		this.fullRefresh = fullRefresh;
	}
	
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	
	public void performRequest(IRequestListener<PubEventArray> listener, HashMap<Long, PubEventArray> storedData) {
		Log.d(Constants.MsgInfo, "Running refresh");
		
		Document xmlRequest = new Document();
		Element root = new Element("Message");
		xmlRequest.setRootElement(root);
		
		Element messageTypeElement = new Element(MessageType.class.getSimpleName());
		messageTypeElement.addContent(MessageType.refreshMessage.toString());
		root.addContent(messageTypeElement);
		
		RefreshData refreshMessage = new RefreshData(service.GetActiveUser(), fullRefresh);
		root.addContent(refreshMessage.writeXml());
		
		
		Document returnDocument;
		
		try {
			returnDocument = DataSender.sendReceiveDocument(xmlRequest);
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		}
		
		RefreshResponse response = new RefreshResponse(returnDocument.getRootElement().getChild(RefreshResponse.class.getSimpleName()));
		
		HashMap<PubEvent, UpdateType> events = new HashMap<PubEvent, UpdateType>();
		
		for(Integer eventUpdate : response.getEvents())
		{
			RefreshEventMessage refreshEventMessage = new RefreshEventMessage(eventUpdate, service.GetActiveUser());
			Element rootElement = new Element("Message");
			
			Element messageTElement = new Element(MessageType.class.getSimpleName());
			messageTElement.addContent(MessageType.refreshEventMessage.toString());
			rootElement.addContent(messageTElement);
			
			rootElement.addContent(refreshEventMessage.writeXml());
			Document refreshEventDocToSend = new Document(rootElement);
			
			try {
				Document pubReturnDocument = DataSender.sendReceiveDocument(refreshEventDocToSend);
				RefreshEventResponseMessage returnMessage = new RefreshEventResponseMessage(pubReturnDocument.getRootElement().
						getChild(RefreshEventResponseMessage.class.getSimpleName()));
				
				events.put(returnMessage.getEvent(), returnMessage.getUpdateType());
				//events.put(new PubEvent(pubReturnDocument.getRootElement().getChild(PubEvent.class.getSimpleName())), eventUpdate.getValue());
			} catch (Exception e) {
				listener.onRequestFail(e);
			}
		}
		
		PubEventArray pubArray = new PubEventArray(events);
		
		service.ReceiveEvents(pubArray);
		
		listener.onRequestComplete(pubArray);
	}

	
	public String getStoredDataId() {
		return Constants.NoDictionaryForGenericDataStore;
	}

}

package dimappers.android.pub;

import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshEventMessage;

public class DataRequestGetLatestAboutPubEvent implements IDataRequest<Integer, PubEvent> {

	IPubService service;
	int eventId;
	
	public DataRequestGetLatestAboutPubEvent(int eventId) {this.eventId = eventId;}
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<PubEvent> listener,	HashMap<Integer, PubEvent> storedData) {
		
		RefreshEventMessage refreshEventMessage = new RefreshEventMessage(eventId);
		Element rootElement = new Element("Message");
		
		Element messageTElement = new Element(MessageType.class.getSimpleName());
		messageTElement.addContent(MessageType.refreshEventMessage.toString());
		rootElement.addContent(messageTElement);
		
		rootElement.addContent(refreshEventMessage.writeXml());
		Document refreshEventDocToSend = new Document(rootElement);
		
		try {
			Document pubReturnDocument = DataSender.sendReceiveDocument(refreshEventDocToSend);
			PubEvent event = new PubEvent(pubReturnDocument.getRootElement().getChild(PubEvent.class.getSimpleName()));
			listener.onRequestComplete(event);
			storedData.put(eventId, event);
		} catch (Exception e) {
			listener.onRequestFail(e);
		}
	}

	public String getStoredDataId() {
		return "PubEvent";
	}

}

package dimappers.android.pub;

import java.util.HashMap;

import net.awl.appgarden.sdk.AppGardenAgent;

import org.jdom.Document;
import org.jdom.Element;
import dimappers.android.PubData.ConfirmMessage;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshEventResponseMessage;
import dimappers.android.PubData.UpdateType;

public class DataRequestConfirmDeny implements IDataRequest<Integer, PubEvent> {

	PubEvent event;
	
	IPubService service;
	
	public DataRequestConfirmDeny(PubEvent event)
	{
		AppGardenAgent.passExam("DATAREQUESTCONFIRMDENY INITIALISED");
		this.event = event;
	}
	
	
	public void giveConnection(IPubService connectionInterface) {
		// TODO Auto-generated method stub
		service = connectionInterface;
	}

	
	public void performRequest(IRequestListener<PubEvent> listener,
			HashMap<Integer, PubEvent> storedData) {
		ConfirmMessage cMessage = new ConfirmMessage(event.getCurrentStatus(), event.GetEventId());
		
		Document docToSend = new Document();
		Element root = new Element("Message");
		Element messageTypeElement = new Element(MessageType.class.getSimpleName());
		messageTypeElement.setText(MessageType.confirmMessage.toString());
		root.addContent(messageTypeElement);
		root.addContent(cMessage.writeXml());
		
		docToSend.setRootElement(root);
		
		Document returnDocument;
		
		try {
			returnDocument = DataSender.sendReceiveDocument(docToSend);
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		}
		
		RefreshEventResponseMessage returnMessage = new RefreshEventResponseMessage(returnDocument.getRootElement().
				getChild(RefreshEventResponseMessage.class.getSimpleName()));
		
		PubEvent event = returnMessage.getEvent();
		
		HashMap<PubEvent, UpdateType> update = new HashMap<PubEvent, UpdateType>();
		update.put(event,  returnMessage.getUpdateType());
		service.NewEventsRecieved(new PubEventArray(update));
		
		listener.onRequestComplete(event);	
	}

	
	public String getStoredDataId() {
		return Constants.NoDictionaryForGenericDataStore;
	}

}

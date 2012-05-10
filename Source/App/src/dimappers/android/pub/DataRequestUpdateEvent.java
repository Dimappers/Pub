package dimappers.android.pub;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshEventResponseMessage;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.UpdateType;
import dimappers.android.PubData.User;

public class DataRequestUpdateEvent implements IDataRequest<Long, PubEvent> {

	private IPubService service;	
	private UpdateData data;
	
	public DataRequestUpdateEvent(PubEvent updatedEvent, Collection<User> newUsers)
	{
		data = new UpdateData(updatedEvent.GetEventId(), updatedEvent.GetStartTime(), updatedEvent.GetPubLocation());
		for(User u : newUsers)
		{
				data.addUser(u);
		}
	}
	
	
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	
	
	public void performRequest(IRequestListener<PubEvent> listener,
			HashMap<Long, PubEvent> storedData) {
		Document xmlDoc = new Document();
		Element root= new Element("Message");
		
		Element messageTypeElement = new Element(MessageType.class.getSimpleName());
		messageTypeElement.addContent(MessageType.updateMessage.toString());
		root.addContent(messageTypeElement);
	
		root.addContent(data.writeXml());
		
		xmlDoc.setRootElement(root);
		//TODO: Put port in stream here
		
		Document returnDocument;
		
		try {
			returnDocument = DataSender.sendReceiveDocument(xmlDoc);
		} catch (IOException e) {
			listener.onRequestFail(e);
			return;
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
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

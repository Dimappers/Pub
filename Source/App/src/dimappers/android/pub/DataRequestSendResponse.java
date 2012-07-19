package dimappers.android.pub;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshEventResponseMessage;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateType;
import dimappers.android.PubData.User;

public class DataRequestSendResponse implements IDataRequest<Long, PubEvent> {

	private IPubService service;
	private boolean isGoing;
	private Calendar freeFromWhen;
	private String msgToHost;
	private int eventId;
	private User user;
	
	public DataRequestSendResponse(boolean isGoing, int eventId, Calendar freeFromWhen, String msgToHost)
	{
		this.isGoing = isGoing;
		this.eventId = eventId;
		this.freeFromWhen = freeFromWhen;
		this.msgToHost = msgToHost;
		this.user = service.GetActiveUser();
	}
	
	public DataRequestSendResponse(boolean isGoing, int eventId)
	{
		this(isGoing, eventId, null, "");
	}
	
	public DataRequestSendResponse(ResponseData rd)
	{
		isGoing = rd.GetIsGoing();
		freeFromWhen = rd.GetFreeFromWhen();
		msgToHost = rd.GetMsgToHost();
		eventId = rd.GetEventId();
		user = rd.GetUser();
	}
	
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	
	
	public void performRequest(IRequestListener<PubEvent> listener,
			HashMap<Long, PubEvent> storedData) {
		ResponseData rData = new ResponseData(user, eventId, isGoing, freeFromWhen, msgToHost);
		
		Element root = new Element("Message");
		Document xmlDoc = new Document();
		xmlDoc.setRootElement(root);
		
		Element messageTypeElement = new Element(MessageType.class.getSimpleName());
		messageTypeElement.setText(MessageType.respondMessage.toString());
		
		root.addContent(messageTypeElement);
		root.addContent(rData.writeXml());
		
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
		
		RefreshEventResponseMessage response = new RefreshEventResponseMessage(returnDocument.getRootElement().getChild(RefreshEventResponseMessage.class.getSimpleName()));
		
		PubEvent event = response.getEvent();
		
		HashMap<PubEvent, UpdateType> update = new HashMap<PubEvent, UpdateType>();
		update.put(event,  response.getUpdateType());
		service.NewEventsRecieved(new PubEventArray(update));
		
		listener.onRequestComplete(event);
		
	}

	
	
	public String getStoredDataId() {
		return Constants.NoDictionaryForGenericDataStore;
	}

}


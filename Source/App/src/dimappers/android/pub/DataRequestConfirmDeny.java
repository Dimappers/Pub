package dimappers.android.pub;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dimappers.android.PubData.ConfirmMessage;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;

public class DataRequestConfirmDeny implements IDataRequest<Integer, PubEvent> {

	PubEvent event;
	public DataRequestConfirmDeny(PubEvent event)
	{
		this.event = event;
	}
	
	public void giveConnection(IPubService connectionInterface) {
		// TODO Auto-generated method stub
		
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
		
		
		try {
			DataSender.sendDocument(docToSend);
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		}
		
		listener.onRequestComplete(null);
		/*Document receivedDoc;
		
		try {
			receivedDoc = DataSender.sendReceiveDocument(docToSend);
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		}*/
		
		
	}

	public String getStoredDataId() {
		return Constants.NoDictionaryForGenericDataStore;
	}

}

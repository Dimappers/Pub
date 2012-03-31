package dimappers.android.pub;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;

public class DataRequestNewEvent implements IDataRequest<Integer, PubEvent>
{
	PubEvent eventToSend;
	IPubService service;
	public DataRequestNewEvent(PubEvent event)
	{
		eventToSend = event;
	}
	
	public void giveConnection(IPubService connectionInterface)
	{
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<PubEvent> listener,
			HashMap<Integer, PubEvent> storedData)
	{
		Element root = new Element("Message");
		Document xmlDoc = new Document(root);

		Element messageTypeElement = new Element(MessageType.class.getSimpleName());
		messageTypeElement.addContent(MessageType.newPubEventMessage.toString());
		root.addContent(messageTypeElement);
		root.addContent(eventToSend.writeXml());
		
		Document readDoc;
		try {
			readDoc = DataSender.sendReceiveDocument(xmlDoc);
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		}
		
		AcknoledgementData ackData = new AcknoledgementData(readDoc.getRootElement().getChild(AcknoledgementData.class.getSimpleName()));
		
		eventToSend.SetEventId(ackData.globalEventId);
		storedData.put(ackData.globalEventId, eventToSend);
		
		listener.onRequestComplete(eventToSend);
	}

	public String getStoredDataId()
	{
		return "PubEvent";
	}

}

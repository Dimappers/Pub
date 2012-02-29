package dimappers.android.pub;

import java.io.IOException;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;

public class NewEventDataRequest implements IDataRequest<Integer, PubEvent>
{
	PubEvent eventToSend;
	IPubService service;
	public NewEventDataRequest(PubEvent event)
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


		Element messageTypeElement = new Element("MessageType");
		messageTypeElement.addContent(MessageType.newPubEventMessage.toString());
		root.addContent(messageTypeElement);
		root.addContent(eventToSend.writeXml());
		
		//TODO: Put port in stream here
		
		XMLOutputter outputter = new XMLOutputter();
		try {
			outputter.output(xmlDoc, System.out);
		} catch (IOException e) {
			listener.onRequestFail(e);
			return;
		}
		
		//TODO: Replace with input stream 
		SAXBuilder xmlBuilder = new SAXBuilder();
		Document returnDocument;
		try
		{
			returnDocument = xmlBuilder.build(System.in);
		} catch (Exception e)
		{
			listener.onRequestFail(e);
			return;
		}
		
		Element ackDataElement = returnDocument.getRootElement().getChild(AcknoledgementData.class.getSimpleName());
		
		AcknoledgementData ackData = new AcknoledgementData(ackDataElement);
		
		eventToSend.SetEventId(ackData.globalEventId);
		storedData.put(ackData.globalEventId, eventToSend);
		
		listener.onRequestComplete(eventToSend);
	}

	public String getStoredDataId()
	{
		return "PubEvent";
	}

}

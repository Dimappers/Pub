package dimappers.android.pub;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshData;

public class DataReceiver
{
	private PubService service;
	private PerformRefresh refresher;
	private final long UpdateFrequency = 900000; //Check every 15 minutes
	
	
	public DataReceiver(PubService service)
	{
		this.service = service;
		Timer updateScheduler = new Timer();
		refresher = new PerformRefresh();
		updateScheduler.schedule(refresher, 0, UpdateFrequency);
	}
	
	public void forceUpdate(boolean fullUpdate)
	{
		refresher.doUpdate(fullUpdate);
	}
	
	
	class PerformRefresh extends TimerTask
	{
		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			doUpdate(false);
		}
		
		//TODO: Test me once we can get xmls from the server
		public void doUpdate(boolean fullUpdate)
		{
			RefreshData refreshRequest = new RefreshData(DataReceiver.this.service.getUser(), fullUpdate);
			
			Document document = new Document();
			Element root = new Element("Message");
			Element messageType = new Element("MessageType");
			messageType.addContent(MessageType.refreshMessage.toString());
			root.addContent(messageType);
			root.addContent(refreshRequest.writeXml());
			document.setRootElement(root);
			
			XMLOutputter outputter = new XMLOutputter();
			try
			{
				//Replace System.out with a out stream for the port
				outputter.output(document, System.out);
			} catch (IOException e)
			{
				Log.d(Constants.MsgError, "IO Exception writing xml to stream");
			}
			
			
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document returnDocument;
			try
			{
				returnDocument = xmlBuilder.build(System.in);
			} catch (JDOMException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} //TODO: Replace with input stream 
			
			Element eventsRoot = returnDocument.getRootElement().getChild("PubEvents");
			
			List<Element> eventsElements = eventsRoot.getChildren(PubEvent.class.getSimpleName());
			for(Element eventElement : eventsElements)
			{
				PubEvent newEvent = new PubEvent(eventElement);
				service.getDataStore().AddNewInvitedEvent(newEvent);
			}
		}
		
	}
}

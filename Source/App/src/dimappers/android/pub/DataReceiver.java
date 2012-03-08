package dimappers.android.pub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.User;

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
			doUpdate(false);
		}
		
		//TODO: Test me once we can get xmls from the server
		public void doUpdate(boolean fullUpdate)
		{
			/*RefreshData refreshRequest = new RefreshData(DataReceiver.this.service.getUser(), fullUpdate);
			
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
			ArrayList<PubEvent> newEvents = new ArrayList<PubEvent>();
			for(Element eventElement : eventsElements)
			{
				PubEvent newEvent = new PubEvent(eventElement);
				service.getDataStore().AddNewInvitedEvent(newEvent);
				newEvents.add(newEvent);
			}*/
			
			//Not working or maybe is cound't debug
			ArrayList<PubEvent> newEvents; 
			newEvents = new ArrayList<PubEvent>();
			newEvents.add(new PubEvent(Calendar.getInstance(), new User(124L)));
			
			if(newEvents.size() > 0)
			{
				NotificationManager nManager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);
				if(newEvents.size() == 1)
				{
					Notification newNotification = new Notification(R.drawable.icon, "New pub event", System.currentTimeMillis());
					Context context = service.getApplicationContext();
					Intent notificationIntent = new Intent(context, UserInvites.class);
					Bundle b = new Bundle();
					b.putSerializable(Constants.CurrentWorkingEvent, newEvents.get(0));
					b.putSerializable(Constants.CurrentFacebookUser, service.user);
					notificationIntent.putExtras(b);
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
					
					newNotification.setLatestEventInfo(context, "New Pub Event", newEvents.get(0).toString(), contentIntent);
					
					nManager.notify(1, newNotification);
				}
				else
				{
					
				}
			}
			
			
		}
		
	}
}

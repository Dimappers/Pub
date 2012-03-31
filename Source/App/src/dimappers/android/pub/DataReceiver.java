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
	private IPubService service;
	private PerformRefresh refresher;
	private final long UpdateFrequency = 900000; //Check every 15 minutes
	
	
	public DataReceiver(IPubService service)
	{
		this.service = service;
		Timer updateScheduler = new Timer();
		refresher = new PerformRefresh();
		refresher.doUpdate(true); //when we start the receiver do a full update to get back up to speed
		updateScheduler.schedule(refresher, UpdateFrequency, UpdateFrequency);
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
			DataRequestRefresh refresh = new DataRequestRefresh(fullUpdate);
			DataReceiver.this.service.addDataRequest(refresh, new IRequestListener<PubEventArray>() {


				public void onRequestComplete(PubEventArray data) {
					if(data.getEvents().size() > 0)
					{
						service.NewEventsRecieved(data);
					}
				}


				public void onRequestFail(Exception e) {
					//Don't know?					
				}
				
			});
		}
		
	}
}

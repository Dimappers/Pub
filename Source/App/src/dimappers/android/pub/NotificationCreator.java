package dimappers.android.pub;

import java.util.ArrayList;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.UpdateType;

public class NotificationCreator {

	private NotificationCreator()
	{
		
	}
	
	/* The way this is structued, each event will create a new notification
	 * I think is standard circumstances, in the unlikely event you get invited
	 * to two pub trips, receiving two notifications seems reasonable
	 * If you disagree, can change methods to take arrays and then have max one
	 * notification per message type
	 * 
	 * Also not sure re: last message, seems like user may want to know if 
	 * event was confirmed before they changed the details. Not sure in general
	 * how this will work
	 */
	
	public static Notification createNotification(UpdateType updateType, PubEvent event, Context context)
	{
		switch(updateType)
		{
		case confirmed:
			return confirmedEventNotification(event, context);
		case confirmedUpdated:
			return updatedEventConfirmedNotification(event, context);
		case newEvent:
			return newEventNotification(event, context);
		case newEventConfirmed:
			return newEventConfirmedNotification(event, context);
		case noChangeSinceLastUpdate:
			return null;
		case updatedConfirmed:
			return updatedEventConfirmedNotification(event, context);
		case updatedEvent:
			return updatedEventNotification(event, context);
		case userReplied:
			return null;		
		}
		
		Log.d(Constants.MsgError, "Unknown notification type ");
		return null;
	}
	
	private static Notification newEventNotification(PubEvent event, Context context)
	{
		/*
		 * Notification newNotification = new Notification(R.drawable.icon, "New pub event", System.currentTimeMillis());
				newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				newNotification.defaults |= Notification.DEFAULT_VIBRATE;
				Intent notificationIntent = new Intent(context, LaunchApplication.class);
				Bundle b = new Bundle();
				PubEvent event = events.getEvents().keySet().iterator().next();
				b.putSerializable(Constants.CurrentWorkingEvent, event.GetEventId());
				notificationIntent.putExtras(b);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				
				newNotification.setLatestEventInfo(context, "New Pub Event", event.toString(), contentIntent);
				
				nManager.notify(1, newNotification);
		 */
		Notification newNotification = new Notification(R.drawable.icon, "New pub event", System.currentTimeMillis());
		newNotification.defaults |= Notification.DEFAULT_VIBRATE;
		Intent notificationIntent = new Intent(context, UserInvites.class);
		
		Bundle b = new Bundle();
		b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
		notificationIntent.putExtras(b);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		newNotification.setLatestEventInfo(context, "New Pub Event", event.toString(), contentIntent);
		
		return newNotification;
	}
	
	private static Notification updatedEventNotification(PubEvent event, Context context)
	{
		Notification newNotification = new Notification(R.drawable.icon, "Change of plan", System.currentTimeMillis());
		newNotification.defaults |= Notification.DEFAULT_VIBRATE;
		Intent notificationIntent = new Intent(context, UserInvites.class);
		
		Bundle b = new Bundle();
		b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
		notificationIntent.putExtras(b);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		newNotification.setLatestEventInfo(context, "Change of plan", event.toString(), contentIntent);
		
		return newNotification;
	}
	
	private static Notification confirmedEventNotification(PubEvent event, Context context)
	{
		Notification newNotification;
		if(event.getCurrentStatus() == EventStatus.itsOff)
		{
			newNotification = new Notification(R.drawable.icon, "Event cancelled", System.currentTimeMillis());
		}
		else
		{
			newNotification = new Notification(R.drawable.icon, "It's on @ " + event.GetFormattedStartTime() + "!", System.currentTimeMillis());
		}
		 
		newNotification.defaults |= Notification.DEFAULT_VIBRATE;
		Intent notificationIntent = new Intent(context, UserInvites.class);
		
		Bundle b = new Bundle();
		b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
		notificationIntent.putExtras(b);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		if(event.getCurrentStatus() == EventStatus.itsOff)
		{
			newNotification.setLatestEventInfo(context, "Event cancelled", event.toString(), contentIntent);
		}
		else
		{
			newNotification.setLatestEventInfo(context, "It's on!", event.toString(), contentIntent);
		}
		
		return newNotification;
	}
	
	private static Notification newEventConfirmedNotification(PubEvent event, Context context)
	{
		if(event.getCurrentStatus() == EventStatus.itsOn)
		{
			Notification newNotification = new Notification(R.drawable.icon, "New confirmed pub event", System.currentTimeMillis());
			newNotification.defaults |= Notification.DEFAULT_VIBRATE;
			Intent notificationIntent = new Intent(context, UserInvites.class);
			
			Bundle b = new Bundle();
			b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationIntent.putExtras(b);
			
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			newNotification.setLatestEventInfo(context, "New Confirmed Pub Event", event.toString(), contentIntent);
			
			return newNotification;
		}
		else //don't notify if new event that has been cancelled
		{
			return null;
		}
	}
	
	private static Notification updatedEventConfirmedNotification(PubEvent event, Context context)
	{
		if(event.getCurrentStatus() == EventStatus.itsOn)
		{
			Notification newNotification = new Notification(R.drawable.icon, "Change of plan & it's on!", System.currentTimeMillis());
			newNotification.defaults |= Notification.DEFAULT_VIBRATE;
			Intent notificationIntent = new Intent(context, UserInvites.class);
			
			Bundle b = new Bundle();
			b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationIntent.putExtras(b);
			
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			newNotification.setLatestEventInfo(context, "Change of plan & it's on!", event.toString(), contentIntent);
			
			return newNotification;
		}
		else
		{
			Notification newNotification;
			newNotification = new Notification(R.drawable.icon, "Event cancelled", System.currentTimeMillis());
			 
			newNotification.defaults |= Notification.DEFAULT_VIBRATE;
			Intent notificationIntent = new Intent(context, UserInvites.class);
			
			Bundle b = new Bundle();
			b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationIntent.putExtras(b);
			
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			newNotification.setLatestEventInfo(context, "Event cancelled", event.toString(), contentIntent);
			
			return newNotification;
		}
	}
}

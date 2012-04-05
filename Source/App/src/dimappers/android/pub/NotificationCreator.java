package dimappers.android.pub;

import java.util.ArrayList;

import android.app.Notification;
import android.util.Log;
import dimappers.android.PubData.Constants;
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
	
	public static Notification createNotification(UpdateType updateType, PubEvent event)
	{
		switch(updateType)
		{
		case confirmed:
			return confirmedEventNotification(event);
		case confirmedUpdated:
			return confirmedUpdatedNotification(event);
		case newEvent:
			return newEventNotification(event);
		case newEventConfirmed:
			return newEventNotification(event);
		case noChangeSinceLastUpdate:
			return null;
		case updatedConfirmed:
			return updatedEventConfirmedNotification(event);
		case updatedEvent:
			return updatedEventNotification(event);		
		}
		
		Log.d(Constants.MsgError, "Unknown notification type");
		return null;
	}
	
	public static Notification newEventNotification(PubEvent event)
	{
		return null;
	}
	
	public static Notification updatedEventNotification(PubEvent event)
	{
		return null;
	}
	
	public static Notification confirmedEventNotification(PubEvent event)
	{
		return null;
	}
	
	public static Notification newEventConfirmedNotification(PubEvent event)
	{
		return null;
	}
	
	public static Notification updatedEventConfirmedNotification(PubEvent event)
	{
		return null;
	}
	
	public static Notification confirmedUpdatedNotification(PubEvent event)
	{
		return null;
	}
}

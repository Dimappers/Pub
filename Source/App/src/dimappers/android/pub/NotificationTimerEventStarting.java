package dimappers.android.pub;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;

public class NotificationTimerEventStarting extends BroadcastReceiver {
	
	PubEvent event;
	Context context;
	boolean isHost;
	IPubService service;
	/*PubEvent event;
	Notification newNotification;*/

	private Notification eventAboutToStart()
	{
		Notification newNotification = new Notification(
				R.drawable.icon, 
				"The pub trip to " + event.GetPubLocation().getName() + " is starting now.", 
				event.GetStartTime().getTimeInMillis());
		Intent notificationIntent;
		if(isHost)
		{
			notificationIntent = new Intent(context, HostEvents.class);
		}
		else
		{
			notificationIntent = new Intent(context, UserInvites.class);
		}
		Bundle b = new Bundle();
		b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
		notificationIntent.putExtras(b);
		PendingIntent contentIntent = PendingIntent.getActivity(context, event.GetEventId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		newNotification.setLatestEventInfo(
				context, 
				"Pub event at " + event.GetPubLocation().getName(), 
				"This event is now starting.", 
				contentIntent);
		
		//service.EventHasHappenened(event);
		
		return newNotification;
	}



	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		Log.d(Constants.MsgWarning, "NotificationAlarmManager has been fired");

		service = (IPubService) peekService(context, new Intent(context, PubService.class));
		
		if(service!=null)
		{
			event = service.getEvent(intent.getIntExtra(Constants.CurrentWorkingEvent,Integer.MIN_VALUE));
				
			isHost = intent.getBooleanExtra(Constants.CurrentFacebookUser, false);
			
			if(event!=null)
			{
				NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
				Notification newNotification = eventAboutToStart();
	
				if(newNotification!=null)
				{
					newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
					newNotification.defaults |= Notification.DEFAULT_VIBRATE;
	
					nManager.notify(event.GetEventId(), newNotification);
				}
				
				sendAddEventToHistory();
			}
		}
		else
		{
			AssociatedPendingIntents.rescheduleBroadcast(context, intent);
		}

	}

	private void sendAddEventToHistory() 
	{
		service.AddEventToHistory(event);
	}
}

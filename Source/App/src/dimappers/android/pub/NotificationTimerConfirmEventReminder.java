package dimappers.android.pub;

import java.io.Serializable;
import java.util.HashMap;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class NotificationTimerConfirmEventReminder extends BroadcastReceiver {
	IPubService service;
	PubEvent event;
	Notification newNotification;
	
	Context context;

		/*private void eventAboutToStart()
		{
			newNotification = new Notification(
					R.drawable.icon, 
					"The pub trip to " + event.GetPubLocation().toString() + " is starting now.", 
					event.GetStartTime().getTimeInMillis());
			Intent notificationIntent;
			if(event.GetHost().equals(service.GetActiveUser())) {notificationIntent = new Intent(context, HostEvents.class);}
			else {notificationIntent = new Intent(context, UserInvites.class);}
			Bundle b = new Bundle();
			b.putSerializable(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationIntent.putExtras(b);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
			newNotification.setLatestEventInfo(
					context, 
					"Pub event at " + event.GetPubLocation().getName(), 
					"This event is now starting.", 
					contentIntent);
			//service.EventHasHappenened(event);
			service.AddEventToHistory(event);
		}*/

		private void hostClickItsOn()
		{
			if(event.getCurrentStatus() == EventStatus.unknown) //ensure we only prompt the user to confirm the event once
			{
				newNotification = new Notification(
						R.drawable.icon, 
						"The pub trip to " + event.GetPubLocation().getName() + " needs confirming.", 
						System.currentTimeMillis());
	
				Intent notificationIntent = new Intent(context, HostEvents.class);
				Bundle b = new Bundle();
				b.putSerializable(Constants.CurrentWorkingEvent, event.GetEventId());
				notificationIntent.putExtras(b);
	
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
	
				int confirmed = 0;
				HashMap<User, UserStatus> goingStatuses = event.GetGoingStatusMap();
				for(User user : goingStatuses.keySet())
				{
					if(goingStatuses.get(user).equals(GoingStatus.going)) {++confirmed;}
				}
	
				newNotification.setLatestEventInfo(
						context, 
						"Please confirm trip to " + event.GetPubLocation().toString() + " starting " + event.GetFormattedStartTime(), 
						"This event has " + confirmed + " confirmed guest(s).", 
						contentIntent);
			}
		}

		@Override
		public void onReceive(Context context, Intent arg1) {
			
			this.context = context;
			
			Log.d(Constants.MsgWarning, "NotificationTimerConfirmEventReminder has been fired");

			service = (IPubService) peekService(context, new Intent(context, PubService.class));
			event = service.getEvent(arg1.getIntExtra(Constants.CurrentWorkingEvent, Integer.MIN_VALUE));
			
			if(event!=null)
			{

				NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
				hostClickItsOn();
	
				if(newNotification!=null)
				{
					newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
					newNotification.defaults |= Notification.DEFAULT_VIBRATE;
		
					nManager.notify(event.GetEventId(), newNotification);
				}
			}
		}
}

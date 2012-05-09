package dimappers.android.pub;

import java.io.Serializable;
import java.util.HashMap;

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
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;

public class NotificationAlarmManager extends Activity {

	Notification newNotification; 
	PubEvent event;
	IPubService service;
	int eventId;
	Bundle extras;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(Constants.MsgWarning, "NotificationAlarmManager has been fired");
		extras = getIntent().getExtras();

		eventId = extras.getInt(Constants.CurrentWorkingEvent);

		bindService(new Intent(this, PubService.class), connection, 0);
	}

	ServiceConnection connection = new ServiceConnection(){

		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder) {

			service = (IPubService) serviceBinder;
			event = service.getEvent(eventId);
			if(event!=null)
			{
				NotificationType type = (NotificationType)(extras.getSerializable(Constants.RequiredNotificationType));

				NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				switch(type)
				{
				case EventAboutToStart :
				{
					eventAboutToStart();
					break;
				}
				case HostClickItsOnReminder :
				{
					hostClickItsOn();
					break;
				}
				}

				if(newNotification!=null)
				{
					newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
					newNotification.defaults |= Notification.DEFAULT_VIBRATE;

					nManager.notify(event.GetEventId(), newNotification);
				}
			}
			finish();
		}

		public void onServiceDisconnected(ComponentName name) {}};

		private void eventAboutToStart()
		{
			newNotification = new Notification(
					R.drawable.icon, 
					"The pub trip to " + event.GetPubLocation().toString() + " is starting now.", 
					event.GetStartTime().getTimeInMillis());
			Intent notificationIntent;
			if(event.GetHost().equals(service.GetActiveUser())) {notificationIntent = new Intent(getBaseContext(), HostEvents.class);}
			else {notificationIntent = new Intent(getBaseContext(), UserInvites.class);}
			Bundle b = new Bundle();
			b.putSerializable(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationIntent.putExtras(b);
			PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
			newNotification.setLatestEventInfo(
					getBaseContext(), 
					"Pub event at " + event.GetPubLocation().getName(), 
					"This event is now starting.", 
					contentIntent);
			//service.EventHasHappenened(event);
			service.AddEventToHistory(event);
		}

		private void hostClickItsOn()
		{
			newNotification = new Notification(
					R.drawable.icon, 
					"The pub trip to " + event.GetPubLocation().getName() + " needs confirming.", 
					System.currentTimeMillis());

			Intent notificationIntent = new Intent(getBaseContext(), HostEvents.class);
			Bundle b = new Bundle();
			b.putSerializable(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationIntent.putExtras(b);

			PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

			int confirmed = 0;
			HashMap<User, UserStatus> goingStatuses = event.GetGoingStatusMap();
			for(User user : goingStatuses.keySet())
			{
				if(goingStatuses.get(user).equals(GoingStatus.going)) {++confirmed;}
			}

			newNotification.setLatestEventInfo(
					getBaseContext(), 
					"Please confirm trip to " + event.GetPubLocation().toString() + " starting " + event.GetFormattedStartTime(), 
					"This event has " + confirmed + " confirmed guest(s).", 
					contentIntent);
		}

		public void onDestroy()
		{
			super.onDestroy();
			unbindService(connection);
		}

		public enum NotificationType implements Serializable
		{
			HostClickItsOnReminder,
			EventAboutToStart,
		}
}


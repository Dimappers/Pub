package dimappers.android.pub;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class NotificationTimerEventStarting extends Activity{
	IPubService service;
	int eventId;
	/*PubEvent event;
	Notification newNotification;*/
	
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(Constants.MsgWarning, "NotificationAlarmManager has been fired");

		eventId = getIntent().getExtras().getInt(Constants.CurrentWorkingEvent);

		bindService(new Intent(this, PubService.class), connection, 0);
	}

	ServiceConnection connection = new ServiceConnection(){

		
		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder) {

			service = (IPubService) serviceBinder;
			PubEvent event = service.getEvent(eventId);
			if(event!=null)
			{
				NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				Notification newNotification = eventAboutToStart(event);

				if(newNotification!=null)
				{
					newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
					newNotification.defaults |= Notification.DEFAULT_VIBRATE;

					nManager.notify(event.GetEventId(), newNotification);
				}
				
				service.AddEventToHistory(event);
			}
			finish();
		}

		
		public void onServiceDisconnected(ComponentName name) {}};

		private Notification eventAboutToStart(PubEvent event)
		{
			Notification newNotification = new Notification(
					R.drawable.icon, 
					"The pub trip to " + event.GetPubLocation().getName() + " is starting now.", 
					event.GetStartTime().getTimeInMillis());
			Intent notificationIntent;
			if(event.GetHost().equals(service.GetActiveUser())) {notificationIntent = new Intent(getBaseContext(), HostEvents.class);}
			else {notificationIntent = new Intent(getBaseContext(), UserInvites.class);}
			Bundle b = new Bundle();
			b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationIntent.putExtras(b);
			PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), event.GetEventId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			newNotification.setLatestEventInfo(
					getBaseContext(), 
					"Pub event at " + event.GetPubLocation().getName(), 
					"This event is now starting.", 
					contentIntent);
			//service.EventHasHappenened(event);
			
			return newNotification;
		}

		
		public void onDestroy()
		{
			super.onDestroy();
			unbindService(connection);
		}
}

package dimappers.android.pub;

import java.io.Serializable;

import android.app.Activity;
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

	public class NotificationAlarmManager extends Activity {
		
		 Notification newNotification; 
		 PubEvent event;
		
		@Override
	 public void onCreate(Bundle savedInstanceState)
		{
		 super.onCreate(savedInstanceState);
		 Bundle extras = getIntent().getExtras();
		 event = (PubEvent)(extras.getSerializable(Constants.CurrentWorkingEvent));
		 NotificationType type = (NotificationType)(extras.getSerializable(Constants.RequiredNotificationType));

		 NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		 
		 switch(type)
		 {
		 case EventAboutToStart :
		 {
			 eventAboutToStart();
			 break;
		 }
		 default :
		 {
			 Log.d(Constants.MsgWarning, "This reminder has not been implemented.");
			 break;
		 }
		 }
		 
		 if(newNotification!=null)
		 {
			 newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
			 newNotification.defaults |= Notification.DEFAULT_VIBRATE;
			 
			 nManager.notify(event.GetEventId(), newNotification);
		 }
		 
		 Log.d(Constants.MsgWarning, "NotificationAlarmManager has been fired");
		 finish();
	 }
	 
		private void eventAboutToStart()
		{
			newNotification = new Notification(
					R.drawable.icon, 
					"The pub trip to " + event.GetPubLocation().toString() + " is starting now.", 
					event.GetStartTime().getTimeInMillis());
			 Intent notificationIntent = new Intent(getBaseContext(), LaunchApplication.class);
			 Bundle b = new Bundle();
			 b.putSerializable(Constants.CurrentWorkingEvent, event.GetEventId());
			 notificationIntent.putExtras(b);
			 PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
			 newNotification.setLatestEventInfo(
					 getBaseContext(), 
					 "Pub event at " + event.GetPubLocation().toString(), 
					 "This event is now starting.", 
					 contentIntent);
		}
		
		public enum NotificationType implements Serializable
		{
			HostClickItsOnReminder,
			EventAboutToStart
		}
	}
	

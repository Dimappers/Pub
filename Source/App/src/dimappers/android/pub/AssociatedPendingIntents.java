package dimappers.android.pub;

import java.util.Calendar;
import java.util.Date;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.PubEvent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AssociatedPendingIntents {
	PendingIntent deleteIntent;
	
	PendingIntent remindConfirm;
	PendingIntent remindHappening;
	
	PubEvent event;
	boolean isHost;
	
	final long deleteAfterEventTime = 6 * 60 * 60 * 1000; //currently set to 6 hours
	final long hostReminderTime = 2 * 60 * 60 * 1000;//7200000 * 2; //currently set to milliseconds in 2 hours
	
	final AlarmManager alarmManager;
	
	public AssociatedPendingIntents(PubEvent event, boolean isHost, Context context)
	{
		alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		this.event = event;
		this.isHost = isHost;
		
		//Create delete intent
		{
			Intent delIntent = new Intent(context, DeleteOldEventActivity.class);
			Bundle delBundle = new Bundle();
			delBundle.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			delIntent.putExtras(delBundle);
			deleteIntent = PendingIntent.getActivity(context, event.GetEventId(), delIntent, 0);
			
			alarmManager.set(AlarmManager.RTC, event.GetStartTime().getTimeInMillis() + deleteAfterEventTime, deleteIntent);
		}
		
		//Create the start time reminder
		if(event.GetStartTime().after(Calendar.getInstance()))
		{
			Intent notificationAlarmIntent = new Intent(context, NotificationTimerEventStarting.class);
			Bundle remindBundle = new Bundle();
			remindBundle.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			notificationAlarmIntent.putExtras(remindBundle);
			remindHappening  = PendingIntent.getActivity(context, event.GetEventId(), notificationAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
			alarmManager.set(AlarmManager.RTC_WAKEUP, event.GetStartTime().getTimeInMillis(), remindHappening);
		}
		
		if(isHost)
		{
			if(event.GetStartTime().after(Calendar.getInstance()))
			{
				if(event.getCurrentStatus() == EventStatus.unknown)
				{
					//Create the confirm reminder
					Intent notificationConfirmAlarmIntent = new Intent(context, NotificationTimerConfirmEventReminder.class);
					Bundle b = new Bundle();
					b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
					notificationConfirmAlarmIntent.putExtras(b);
					remindConfirm = PendingIntent.getActivity(context, event.GetEventId(), notificationConfirmAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					
					alarmManager.set(AlarmManager.RTC_WAKEUP, event.GetStartTime().getTimeInMillis() - hostReminderTime, remindConfirm);
				}
			}
		}
	}
	
	public void UpdateFromEvent(PubEvent event)
	{
		this.event = event;
		alarmManager.cancel(deleteIntent);
		alarmManager.set(AlarmManager.RTC, event.GetStartTime().getTimeInMillis() + deleteAfterEventTime, deleteIntent);
		
		alarmManager.cancel(remindHappening);
		if(event.GetStartTime().after(Calendar.getInstance()))
		{
			if(event.getCurrentStatus() != EventStatus.itsOff)
			{
				alarmManager.set(AlarmManager.RTC, event.GetStartTime().getTimeInMillis(), remindHappening);
			}
		}
		
		if(isHost)
		{
			alarmManager.cancel(remindConfirm);
			if(event.getCurrentStatus() == EventStatus.unknown)
			{
				if(event.GetStartTime().after(Calendar.getInstance()))
				{
					if(event.getCurrentStatus() == EventStatus.unknown)
					{
						alarmManager.set(AlarmManager.RTC_WAKEUP, event.GetStartTime().getTimeInMillis() - hostReminderTime, remindConfirm);
					}
				}
			}
		}
	}
}

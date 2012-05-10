package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.UpdateType;

public class PubService extends IntentService
{
	//AppUser user;
	
	public PubService() {
		super("PubService");
		hasStarted = false;
	}

	//ServiceBinder is our interface to communicate with the service
	public class ServiceBinder extends Binder implements IPubService {
		
		long hostReminderTime = 7200000; //currently set to milliseconds in 2 hours
		final long deleteAfterEventTime = 6 * 60 * 60 * 1000; //currently set to 6 hours
		
        PubService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PubService.this;
        }

		
		public int GiveNewSavedEvent(PubEvent event) {
			PubService.this.storedData.AddNewSavedEvent(event);
			return event.GetEventId();
		}

		
		public void GiveNewSentEvent(PubEvent event, final IRequestListener<PubEvent> listener) {
			DataRequestNewEvent r = new DataRequestNewEvent(event);
			final int savedEventId = event.GetEventId();
			PubService.this.addDataRequest(r, new IRequestListener<PubEvent>() {

					
					public void onRequestComplete(PubEvent data) {
						storedData.DeleteSavedEvent(savedEventId);
						makeNotification(data, NotificationAlarmManager.NotificationType.EventAboutToStart);
						makeNotification(data, NotificationAlarmManager.NotificationType.HostClickItsOnReminder);
						
						//Register to have the event deleted
						Intent notificationAlarmIntent = new Intent(getApplicationContext(), DeleteOldEventActivity.class);
						Bundle b = new Bundle();
						b.putSerializable(Constants.CurrentWorkingEvent, data.GetEventId());
						notificationAlarmIntent.putExtras(b);
						
						PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

						((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, data.GetStartTime().getTimeInMillis() + deleteAfterEventTime, contentIntent);
						
						
						listener.onRequestComplete(data);
					}

					
					public void onRequestFail(Exception e) {
						listener.onRequestFail(e);
					}
				});
		}

		
		public Collection<PubEvent> GetSavedEvents() {
			return PubService.this.storedData.GetSavedEvents();
		}

		
		public Collection<PubEvent> GetSentEvents() {
			HashMap<?, ? extends IXmlable> events = PubService.this.storedData.GetGenericStore("PubEvent");
			Collection<PubEvent> eventsArray = new ArrayList<PubEvent>();
			for(Object event : events.values())
			{
				eventsArray.add((PubEvent)event);
			}
			
			return eventsArray;
		}
		
		
		public Collection<PubEvent> GetInvitedEvents() {
			return PubService.this.storedData.GetInvitedEvents();
		}

		public Collection<PubEvent> GetAllEvents() {
			return PubService.this.storedData.GetAllEvents();
		}

		
		public PubEvent GetNextEvent() {
			Log.d(Constants.MsgError, "Not implemented get next event message yet");
			return null;
		}

		
		public void RemoveEventFromStoredDataAndCancelNotification(PubEvent event) {
			PubService.this.storedData.DeleteSavedEvent(event.GetEventId());
			/////////////////////////////////////////////////((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(event.GetEventId());
		}
		
		
		public void CancelEvent(final PubEvent event)
		{
			event.setCurrentStatus(EventStatus.itsOff);
			
			DataRequestConfirmDeny cancel = new DataRequestConfirmDeny(event);
			addDataRequest(cancel, new IRequestListener<PubEvent>(){
				
				public void onRequestComplete(PubEvent data)
				{
					//PubService.this.storedData.DeleteSentEvent(event.GetEventId());
					//TODO: Remove notifications for the future
				}
				
				public void onRequestFail(Exception e)
				{
					// TODO Auto-generated method stub
					
				}
				
			});
		}

		
		public void PerformUpdate(boolean fullUpdate) {
			PubService.this.receiver.forceUpdate(fullUpdate);
		}

		
		public Facebook GetFacebook() {
			return PubService.this.authenticatedFacebook;
		}

		
		public void Logout() throws MalformedURLException, IOException {
			//TODO: Implement facebook logout
			GetFacebook().logout(getApplicationContext());
		}

		
		public <K, T extends IXmlable> void addDataRequest(IDataRequest<K, T> request,
				IRequestListener<T> listener)
		{
			PubService.this.addDataRequest(request, listener);			
		}

		
		public AppUser GetActiveUser() {
			return storedData.getActiveUser();
		}

		
		public void NewEventsRecieved(PubEventArray events) {
			
			ArrayList<Notification> notifications = new ArrayList<Notification>();
			ArrayList<Integer> notificationIds = new ArrayList<Integer>();
					
			for(Entry<PubEvent, UpdateType> eventEntry : events.getEvents().entrySet())
			{
				if(eventEntry.getValue()!=UpdateType.noChangeSinceLastUpdate)
				{
					makeNotification(eventEntry.getKey(), NotificationAlarmManager.NotificationType.EventAboutToStart);
				}
				//If either the event hasn't been updated (ie this user has already got this data before and this is a full refresh caused by restarting the app
				PubEvent event = eventEntry.getKey();
				if(event.GetHost().equals(GetActiveUser()))
				{
					storedData.GetGenericStore("PubEvent").put(event.GetEventId(), event);
				}
				else
				{
					storedData.AddNewInvitedEvent(eventEntry.getKey());
				}
				Context context = getApplicationContext();
				Notification newNotification = NotificationCreator.createNotification(eventEntry.getValue(), event, context, GetActiveUser(), GetFacebook());
				if(newNotification != null)
				{
					notifications.add(newNotification);
					notificationIds.add(event.GetEventId());
				}
			}			
			
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			for(int i = 0; i < notifications.size(); ++i)
			{
				nManager.notify(notificationIds.get(i), notifications.get(i));
			}
		}

		
		public HistoryStore getHistoryStore() {
			return storedData.getHistoryStore();
		}

		
		public PubEvent getEvent(int eventId) {
			return storedData.getEvent(eventId);
		} 
    
		private void makeNotification(PubEvent event, NotificationAlarmManager.NotificationType type)
		{
			Intent notificationAlarmIntent = new Intent(getApplicationContext(), NotificationAlarmManager.class);
			Bundle b = new Bundle();
			b.putSerializable(Constants.CurrentWorkingEvent, event.GetEventId());
			b.putSerializable(Constants.RequiredNotificationType, type);
			notificationAlarmIntent.putExtras(b);
			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			switch (type)
			{
				case EventAboutToStart :
				{
					((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, event.GetStartTime().getTimeInMillis(), contentIntent);
					break;
				}
				case HostClickItsOnReminder :
				{
					long time = event.GetStartTime().getTimeInMillis();
					time -= hostReminderTime;
					((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, time, contentIntent);
					
					break;
				}
			}
		}

		
		public <K, V extends IXmlable> HashMap<K, V> GetGenericStore(String key) {
			// TODO Auto-generated method stub
			return storedData.GetGenericStore(key);
		}

		
		public void UpdatePubEvent(PubEvent newEvent)
		{
			if(newEvent.GetEventId() < 0)
			{
				GiveNewSavedEvent(newEvent);
			}
			else
			{
				storedData.GetGenericStore(StoredData.sentEventsStore).put(newEvent.GetEventId(), newEvent);
			}
		}

		
		public void AddEventToHistory(PubEvent event) {
			HistoryStore hStore = getHistoryStore();
			hStore.addEvent(event);
		}
		
		
		public void DeleteSentEvent(PubEvent event)
		{
			storedData.DeleteSentEvent(event.GetEventId());
		}
	}

	
	private final IPubService binder = new ServiceBinder();
	
	private StoredData storedData;
	private boolean hasStarted;
	private DataReceiver receiver;
	private	DataSender sender;
	private Facebook authenticatedFacebook;
	//private HistoryStore historyStore;
 
	
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(!hasStarted)
		{
			Log.d(Constants.MsgInfo, "Service started");
			storedData = new StoredData();
			
			//Load previously stored data
			
			String storedDataString = getSharedPreferences(Constants.SaveDataName, MODE_PRIVATE).getString(Constants.SaveDataName, "");
			if(storedDataString != "")
			{
				Log.d(Constants.MsgInfo, "Loading data for store: " + storedDataString);
				storedData.Load(storedDataString);
			}
			
			if(intent!=null && intent.getExtras()!=null && intent.getExtras().containsKey(Constants.CurrentFacebookUser))
			{
				storedData.setActiveUser((AppUser)intent.getExtras().getSerializable(Constants.CurrentFacebookUser));
				storedData.setAuthKey(intent.getExtras().getString(Constants.AuthToken));
				storedData.setExpiryDate(intent.getExtras().getLong(Constants.Expires));
				//Editor e = getSharedPreferences(Constants.SaveDataName, MODE_PRIVATE).edit();
				//e.putString(Constants.CurrentFacebookUser, Long.toString(user.getUserId());
				//e.commit();
			}			
			AppUser user = binder.GetActiveUser();
			storedData.GetGenericStore("AppUser").put(user.getUserId(), user);
			
			sender = new DataSender();
			receiver = new DataReceiver(binder);
			
			//FIXME: this causes null pointers
			authenticatedFacebook = new Facebook(Constants.FacebookAppId);
			authenticatedFacebook.setAccessToken(storedData.getAuthKey());
			authenticatedFacebook.setAccessExpires(storedData.getExpiryDate());
			
			hasStarted = true;
			
			// Begin retrieving friends
			DataRequestGetFriends getFriends = new DataRequestGetFriends(getApplicationContext());
			addDataRequest(getFriends, new IRequestListener<AppUserArray>() {

				
				public void onRequestComplete(AppUserArray data) {
					for(AppUser user : data.getArray())
					{
						storedData.GetGenericStore("AppUser").put(user.getUserId(), user);
					}
				}

				
				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, "Error getting friends: " + e.getMessage());
				}
			});
		}
		else
		{
			Log.d(Constants.MsgInfo, "Service already running");
		}
	    return START_STICKY;
	}

	
	
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		Log.d(Constants.MsgInfo, "Service bound too");
		return binder;
	}
	
	
	public boolean onUnbind(Intent intent)
	{
		super.onUnbind(intent);
		String xmlString = storedData.save();
		Editor editor = getSharedPreferences(Constants.SaveDataName, MODE_PRIVATE).edit();
		editor.putString(Constants.SaveDataName, xmlString);
		Log.d(Constants.MsgInfo, "Saving: " + xmlString);
		editor.commit();
		
		return false;
	}
	
	
	public void onDestroy()
	{
		Log.d(Constants.MsgError, "onDestroy() in PubService called");
		String xmlString = storedData.save();
		Editor editor = getSharedPreferences(Constants.SaveDataName, MODE_PRIVATE).edit();
		editor.putString(Constants.SaveDataName, xmlString);
		Log.d(Constants.MsgInfo, "Saving: " + xmlString);
		editor.commit();
		
		super.onDestroy();
	}
	
	public StoredData getDataStore()
	{
		return storedData;
	}

	public <K, T extends IXmlable> void addDataRequest(IDataRequest<K, T> request, final IRequestListener<T> listener)
	{
		HashMap<K, T> currentDataStore = storedData.GetGenericStore(request);
		request.giveConnection(binder);
		if(sender != null)
		{
			sender.addRequest(request, listener, currentDataStore);		
		}
		else
		{
			Log.d(Constants.MsgWarning, "err... isn't a active sender...");
		}
	}
	

	
	protected void onHandleIntent(Intent intent) {
		if(hasStarted)
		{
			Log.d(Constants.MsgError, "Service already started...");
		}
		hasStarted = true;
	}
	
	
	
}

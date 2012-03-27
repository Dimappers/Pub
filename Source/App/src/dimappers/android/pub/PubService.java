package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

public class PubService extends IntentService
{
	AppUser user;
	
	public PubService() {
		super("PubService");
		hasStarted = false;
	}

	//ServiceBinder is our interface to communicate with the service
	public class ServiceBinder extends Binder implements IPubService {
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
			PubService.this.addDataRequest(r, listener);
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

		public void RemoveSavedEvent(PubEvent event) {
			PubService.this.storedData.DeleteSavedEvent(event);
			
		}

		public void PerformUpdate(boolean fullUpdate) {
			PubService.this.receiver.forceUpdate(fullUpdate);
		}

		public boolean SendingMessage() {
			//TODO: Should check to see if a new event has been created but hasn't yet been sent
			return false;
		}

		public Facebook GetFacebook() {
			return PubService.this.authenticatedFacebook;
		}

		public void Logout() {
			//TODO: Implement facebook logout
			
		}

		public <K, T extends IXmlable> void addDataRequest(IDataRequest<K, T> request,
				IRequestListener<T> listener)
		{
			PubService.this.addDataRequest(request, listener);			
		}

		public AppUser GetActiveUser() {
			return user;
		}

		public void NewEventsRecieved(PubEvent[] newEvents) {
			for(PubEvent event : newEvents)
			{
				storedData.AddNewInvitedEvent(event);
			}			
			
			NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Context context = PubService.this.getApplicationContext();
			if(newEvents.length == 1)
			{
				Notification newNotification = new Notification(R.drawable.icon, "New pub event", System.currentTimeMillis());
				newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				newNotification.defaults |= Notification.DEFAULT_VIBRATE;
				Intent notificationIntent = new Intent(context, LaunchApplication.class);
				Bundle b = new Bundle();
				b.putSerializable(Constants.CurrentWorkingEvent, newEvents[0].GetEventId());
				notificationIntent.putExtras(b);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				
				newNotification.setLatestEventInfo(context, "New Pub Event", newEvents[0].toString(), contentIntent);
				
				nManager.notify(1, newNotification);
			}
			else
			{
				Notification newNotification = new Notification(R.drawable.icon, newEvents.length + " new events", System.currentTimeMillis());
				newNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				newNotification.defaults |= Notification.DEFAULT_VIBRATE;
				Intent notificationIntent = new Intent(context, CurrentEvents.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				
				newNotification.setLatestEventInfo(context, newEvents.length + " new events", newEvents.length + " new events", contentIntent);
				
				nManager.notify(1, newNotification);
			}
		}

		public HistoryStore getHistoryStore() {
			return storedData.getHistoryStore();
		}

		public PubEvent getEvent(int eventId) {
			return storedData.getEvent(eventId);
		}
    }

	
	private final IPubService binder = new ServiceBinder();
	
	private StoredData storedData;
	private boolean hasStarted;
	private DataReceiver receiver;
	private	DataSender sender;
	private Facebook authenticatedFacebook;
	//private HistoryStore historyStore;
 
	@Override
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
			
			//TODO: this causes null pointers
			user = (AppUser)intent.getExtras().getSerializable(Constants.CurrentFacebookUser);
			
			storedData.GetGenericStore("AppUser").put(user.getUserId(), user);
			
			sender = new DataSender();
			receiver = new DataReceiver(binder);
			
			authenticatedFacebook = new Facebook(Constants.FacebookAppId);
			authenticatedFacebook.setAccessToken(intent.getExtras().getString(Constants.AuthToken));
			authenticatedFacebook.setAccessExpires(intent.getExtras().getLong(Constants.Expires));
			
			hasStarted = true;
			
			// Begin retrieving friends
			DataRequestGetFriends getFriends = new DataRequestGetFriends();
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

	
	@Override
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		Log.d(Constants.MsgInfo, "Service bound too");
		return binder;
	}
	
	@Override
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
	
	@Override
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
	
	public User getUser()
	{
		return user;
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
	

	@Override
	protected void onHandleIntent(Intent intent) {
		if(hasStarted)
		{
			Log.d(Constants.MsgError, "Service already started...");
		}
		hasStarted = true;
	}
}

package dimappers.android.pub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PubService extends IntentService
{
	User user;
	
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

		public int GiveNewSentEvent(PubEvent event) {
			event.SetEventId(Constants.EventIdBeingSent);
			PubService.this.sender.sendEvent(event);
			PubService.this.storedData.AddNewSentEvent(event);
			return event.GetEventId();
		}

		public Collection<PubEvent> GetSavedEvents() {
			return PubService.this.storedData.GetSavedEvents();
		}

		public Collection<PubEvent> GetSentEvents() {
			return PubService.this.storedData.GetSentEvents();
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
		
		public String Save()
		{
			return PubService.this.storedData.save();
		}
		
		public void Load(String loadedData)
		{
			PubService.this.storedData.Load(loadedData);
		}

		public boolean SendingMessage() {
			//TODO: Should check to see if a new event has been created but hasn't yet been sent
			return false;
		}

		@Override
		public Facebook GetFacebook() {
			return PubService.this.authenticatedFacebook;
		}

		@Override
		public void Logout() {
			//TODO: Implement facebook logout
			
		}
		
    }

	
	private final IBinder binder = new ServiceBinder();
	
	private StoredData storedData;
	private boolean hasStarted;
	private DataReceiver receiver;
	private	DataSender sender;
	private Facebook authenticatedFacebook;
	
	private Queue<IDataRequest<?, IRequestListener<?>>> dataRequestQueue;
	private Dictionary<Class<?>, GenericDataStore<Class<?>>> dataStores;
 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(Constants.MsgInfo, "Service started");
		storedData = new StoredData();
		user = (User)intent.getExtras().getSerializable(Constants.CurrentFacebookUser);
		//receiver = new DataReceiver(this);
		//sender = new DataSender(this);
		
		if(!Constants.emulator)
		{
			authenticatedFacebook = new Facebook("153926784723826");
			authenticatedFacebook.setAccessToken(intent.getExtras().getString(Constants.AuthToken));
			authenticatedFacebook.setAccessExpires(intent.getExtras().getLong(Constants.Expires));
		}
	
		dataRequestQueue = new ArrayBlockingQueue<IDataRequest<?, IRequestListener<?>>>(100);
		dataStores = new Hashtable<Class<?>, GenericDataStore<Class<?>>>();
		
		ArrayList<Integer> a = new ArrayList<Integer>();
		addDataRequest(null, a.getClass());
		
	    return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(Constants.MsgInfo, "Service bound too");
		return binder;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public StoredData getDataStore()
	{
		return storedData;
	}

	public <DataType> void addDataRequest(final IRequestListener<DataType> listener, Class<DataType> dataType)
	{
		if(dataStores.get(dataType) == null)
		{
			GenericDataStore<DataType> dataStore = new GenericDataStore<DataType>();
			dataStores.put(dataType, (GenericDataStore<Class<?>>) dataStore);
			ArrayList<Integer>someData = new ArrayList<Integer>();
			someData.add(4);
			dataStore.setStore((DataType) someData);
		}
		GenericDataStore<Class<?>> myDataStore = dataStores.get(dataType);
		DataType castedDataStore = (DataType)myDataStore.getStore();
		System.out.println(myDataStore.toString());
		
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

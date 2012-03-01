package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
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
			NewEventDataRequest r = new NewEventDataRequest(event);
			PubService.this.addDataRequest(r, listener);
		}

		public Collection<PubEvent> GetSavedEvents() {
			return PubService.this.storedData.GetSavedEvents();
		}

		public Collection<PubEvent> GetSentEvents() {
			return (Collection<PubEvent>) PubService.this.dataStores.get("PubEvent").values();
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

		public Facebook GetFacebook() {
			return PubService.this.authenticatedFacebook;
		}

		public void Logout() {
			//TODO: Implement facebook logout
			
		}

		public <K, T> void addDataRequest(IDataRequest<K, T> request,
				IRequestListener<T> listener)
		{
			PubService.this.addDataRequest(request, listener);
			
		}		
    }

	
	private final IPubService binder = new ServiceBinder();
	
	private StoredData storedData;
	private boolean hasStarted;
	private DataReceiver receiver;
	private	DataSender sender;
	private Facebook authenticatedFacebook;
	
	private Queue<IDataRequest<?,?>> dataRequestQueue;
	private Dictionary<String, HashMap<?,?>> dataStores;
 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(Constants.MsgInfo, "Service started");
		storedData = new StoredData();
		user = (AppUser)intent.getExtras().getSerializable(Constants.CurrentFacebookUser);
		//data.put(facebookIdToGet, appUser);
		//receiver = new DataReceiver(this);
		sender = new DataSender();
		
		if(!Constants.emulator)
		{
			authenticatedFacebook = new Facebook(Constants.FacebookAppId);
			authenticatedFacebook.setAccessToken(intent.getExtras().getString(Constants.AuthToken));
			authenticatedFacebook.setAccessExpires(intent.getExtras().getLong(Constants.Expires));
		}
	
		dataRequestQueue = new ArrayBlockingQueue<IDataRequest<?,?>>(100);
		dataStores = new Hashtable<String, HashMap<?,?>>();
		
		ArrayList<Integer> a = new ArrayList<Integer>();
		
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

	public <K, T> void addDataRequest(IDataRequest<K, T> request, final IRequestListener<T> listener)
	{
		HashMap<K, T> currentDataStore = null;
		if(request.getStoredDataId() != null)
		{
			currentDataStore = (HashMap<K, T>) dataStores.get(request.getStoredDataId()); 
			if(currentDataStore == null )
			{
				currentDataStore = new HashMap<K, T>();
				dataStores.put(request.getStoredDataId(), currentDataStore);
			}
		}
		request.giveConnection(binder);
		
		sender.addRequest(request, listener, currentDataStore);		
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

package dimappers.android.pub;

import java.util.Collection;
import java.util.TimerTask;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PubService extends IntentService
{
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

		@Override
		public int GiveNewSavedEvent(PubEvent event) {
			PubService.this.storedData.AddNewSavedEvent(event);
			return event.GetEventId();
		}

		@Override
		public int GiveNewSentEvent(PubEvent event) {
			PubService.this.storedData.AddNewSentEvent(event);
			return event.GetEventId();
		}

		@Override
		public Collection<PubEvent> GetSavedEvents() {
			return PubService.this.storedData.GetSavedEvents();
		}

		@Override
		public Collection<PubEvent> GetSentEvents() {
			return PubService.this.storedData.GetSentEvents();
		}

		@Override
		public Collection<PubEvent> GetAllInvited() {
			return PubService.this.storedData.GetAllEvents();
		}

		@Override
		public PubEvent GetNextEvent() {
			Log.d(Constants.MsgError, "Not implemented get next event message yet");
			return null;
		}

		@Override
		public void RemoveSavedEvent(PubEvent event) {
			PubService.this.storedData.DeleteSavedEvent(event);
			
		}
    }

	
	private final IBinder binder = new ServiceBinder();
	
	private StoredData storedData;
	private boolean hasStarted;
 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
	    return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}
	
	
	
	class GetUpdates extends TimerTask
	{

		@Override
		public void run()
		{
			int icon = R.drawable.icon;
			CharSequence tickerText = "Hello";
			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);
			PendingIntent p = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
			notification.setLatestEventInfo(getApplicationContext(), "Hello2", "Hello3", p);
			NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			manager.notify("MyNotte", 42, notification);
			
			//TODO: Here we want to send query to server to find out if new events and tell the app if there is 
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
	
	public static IPubService bindToServiceInterface(Activity launchingActivity)
	{
		class PubServiceConnection implements ServiceConnection
		{
			IPubService service;
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				this.service = (IPubService)service;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				
			}
			
			public IPubService getService()
			{
				return service;
			}
			
		}
		PubServiceConnection connection = new PubServiceConnection();
		launchingActivity.bindService(new Intent(launchingActivity, PubService.class), connection, 0);
		return connection.getService();
	}
}

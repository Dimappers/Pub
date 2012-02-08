package dimappers.android.pub;

import java.util.Timer;
import java.util.TimerTask;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class PubService extends IntentService
{

	boolean alreadyRunning = false;
	private static final long UpdateFrequency = 900000; //Check every 15 minutes
	public PubService()
	{
		super("PubService");
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		// Should be nothing here - for if we want to do some service that multiple apps can use
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		//Only want this service to schedule one update
		if(!alreadyRunning)
		{
			alreadyRunning = true;
			
			Timer updater = new Timer();
			
			updater.schedule(new GetUpdates(), 0, UpdateFrequency);
		}
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
}

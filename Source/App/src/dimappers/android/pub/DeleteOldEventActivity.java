package dimappers.android.pub;

import dimappers.android.PubData.Constants;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class DeleteOldEventActivity extends Activity {
	
	private int eventId; //event to delete
	IPubService service;	
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		eventId = getIntent().getExtras().getInt(Constants.CurrentWorkingEvent);
		
		bindService(new Intent(getApplicationContext(), PubService.class), connection, 0);
	}

	
	ServiceConnection connection = new ServiceConnection()
	{
		
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			service = (IPubService)binder;
			
			service.DeleteEvent(service.getEvent(eventId));
			
			finish();
		}
		
		
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}

}

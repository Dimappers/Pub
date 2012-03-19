package dimappers.android.pub;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class Pending extends Activity implements OnClickListener {

	private TextView progressText;
	IPubService service;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pending_guests);
		
		//Bind to service
    	bindService(new Intent(this, PubService.class), connection, 0);
		
		progressText = (TextView) findViewById(R.id.location_error);
		((TextView) findViewById(R.id.cancelbutton)).setOnClickListener(this);
	}

	
	private void findLocation() {
		updateText("Finding current location");
		
		LocationFinder lc = new LocationFinder((LocationManager)getSystemService(Context.LOCATION_SERVICE));
		
		//Find the users current location - required for all other tasks
		lc.findLocation(locationListener);
	}
	
	public void updateText(String s) {
		runOnUiThread(new TextUpdater(s));
	}
	
	class TextUpdater implements Runnable
	{
		String s;

		public void run() {
			progressText.setText(s);
		}
		
		TextUpdater(String s)
		{
			this.s = s;
		}
	}

	public PubEvent createEvent() {
		updateText("Creating Event");

		Bundle b = getIntent().getExtras();
		if (b == null) {
			Debug.waitForDebugger();
		}

		return new PubEvent(new TimeFinder(service.getHistoryStore()).chooseTime(), service.GetActiveUser());
	}

	public void onClick(View v) {
		if (v.getId() == R.id.cancelbutton) {finish();}
	}

	public void onFinish(PubEvent createdEvent, Location currentLocation) {
		setResult(Activity.RESULT_OK, new Intent().putExtras(fillBundle(createdEvent, currentLocation)));
		finish();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}

	private Bundle fillBundle(PubEvent createdEvent, Location currentLocation) {
		service.GiveNewSavedEvent(createdEvent);
		Bundle eventBundle = new Bundle();
		eventBundle.putInt(Constants.CurrentWorkingEvent, createdEvent.GetEventId());
		eventBundle.putBoolean(Constants.IsSavedEventFlag, true);
		eventBundle.putDouble(Constants.CurrentLatitude,
				currentLocation.getLatitude());
		eventBundle.putDouble(Constants.CurrentLongitude,
				currentLocation.getLongitude());
		return eventBundle;
	}

	public void errorOccurred() {
		runOnUiThread(new ErrorDialog());
	}
	
	class ErrorDialog implements Runnable
	{
		public void run() {
			new AlertDialog.Builder(Pending.this)
			.setMessage(
					"An unexpected error has occurred. Please try again.")
			.setTitle("Error").setCancelable(false)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					finish();
				}
			}).show();
		}
	}
	
	private ServiceConnection connection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder pubService)
		{
			//Give the interface to the app
			service = (IPubService)pubService;
			
			//Find the location of the pub
			findLocation();
		}

		public void onServiceDisconnected(ComponentName className)
		{
		}
		
	};
	
	private LocationListener locationListener = new LocationListener()
	{
		private boolean peopleFound = false;
		private boolean pubFound = false;
		
		private List<Place> pubs = null;
		private AppUser[] allFriends = null;
		
		Location currentLocation;
		
		public void onLocationChanged(Location location) //we get the location
		{
			currentLocation = location;
			//Start tasks: Get people & get pubs
			PersonFinder personFinder = new PersonFinder(service);
			Pending.this.updateText("Finding friends");
			
			DataRequestPubFinder pubFinder = new DataRequestPubFinder(currentLocation.getLatitude(), currentLocation.getLongitude());
			service.addDataRequest(pubFinder, new IRequestListener<PlacesList>(){

				public void onRequestComplete(PlacesList data) {
					pubFound = true;
					pubs = data.results;
					if(peopleFound)
					{
						rankThings(pubs);
					}
					else
					{
						Pending.this.updateText("Finding people");
					}
				}

				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, e.getMessage());
					errorOccurred();
				}});
			
			
			personFinder.getFriends(new IRequestListener<AppUserArray>() {
				public void onRequestComplete(AppUserArray data) {
					peopleFound = true;
					allFriends = data.getArray();
					if(pubFound)
					{
						rankThings(pubs);
					}
					else
					{
						Pending.this.updateText("Finding pubs");
					}
				}

				public void onRequestFail(Exception e) {
					Pending.this.errorOccurred();
				}
				
			});
		}
		
		private void rankThings(final List<Place> pubs)
		{
			//Start next batch of requests
			PubEvent event = createEvent();
			PersonRanker p = new PersonRanker(event, service, currentLocation, allFriends, new IRequestListener<PubEvent>() {

				public void onRequestComplete(PubEvent data) {
					data.SetPubLocation(new PubRanker(pubs, data, service.getHistoryStore()).returnBest());
					Pending.this.onFinish(data, currentLocation);
				}

				public void onRequestFail(Exception e) {
					errorOccurred();
				}
				
			});
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
}

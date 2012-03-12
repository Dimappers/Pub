package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;
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
import android.os.AsyncTask;
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
import dimappers.android.PubData.User;

public class Pending extends Activity implements OnClickListener {

	private TextView text;
	private User facebookUser;
	public ArrayList<User> facebookFriends = new ArrayList<User>();
	User[] allFriends;
	private Location currentLocation;

	private boolean firstTime = true;

	PubEvent event;

	boolean personFinished;
	boolean pubFinished;
	private List<Place> pubPlaces;
	
	boolean locationFound = false;
	boolean serviceConnected = false;
	IPubService service;

	@Override
	public void onCreate(Bundle savedInstanceState) {
    	//Bind to service
    	bindService(new Intent(this, PubService.class), connection, 0);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pending_guests);
		text = (TextView) findViewById(R.id.location_error);
		((TextView) findViewById(R.id.cancelbutton)).setOnClickListener(this);
	}

	
	private void findLocation() {
		updateText("Finding current location");
		
		LocationFinder lc = new LocationFinder((LocationManager)getSystemService(Context.LOCATION_SERVICE));
		
		//Find the users current location - required for all other tasks
		lc.findLocation(locationListener);
	}
	
	/*public void continueGoing(Location location) {
		currentLocation = location;
		locationFound = true;
		if(serviceConnected) {startTasks();}
	}*/
	
	public void updateText(String s) {
		text.setText(s);
	}

	/*public void startTasks() {

		if (currentLocation == null) {
			Log.d(Constants.MsgError, "Need to set location first.");
			updateText("An error has occurred, please try again.");
			return;
		} else {
			Log.d(Constants.MsgInfo,
					"Using location: " + currentLocation.getLatitude() + ", "
							+ currentLocation.getLongitude());
		}

		createEvent();

		Object[] info = new Object[2];
		info[0] = currentLocation;
		info[1] = this;

		new PubFinding().execute(info);
		
		new PersonFinder(this, service).getFriends();
	}*/

	public void createEvent() {
		updateText("Creating Event");

		Bundle b = getIntent().getExtras();
		if (b == null) {
			Debug.waitForDebugger();
		}

		facebookUser = (AppUser) b
				.getSerializable(Constants.CurrentFacebookUser);
		event = new PubEvent(new TimeFinder().chooseTime(), facebookUser);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.cancelbutton) {finish();}
	}

	//TODO: remove me
	public void onFinish() {
		//event = new PersonRanker(event, service, allFriends).getEvent();
		event.SetPubLocation(new PubRanker(pubPlaces, event).returnBest());
		setResult(Activity.RESULT_OK, new Intent().putExtras(fillBundle()));
		finish();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}

	private Bundle fillBundle() {
		Bundle eventBundle = new Bundle();
		eventBundle.putAll(getIntent().getExtras());
		eventBundle.putSerializable(Constants.CurrentWorkingEvent, event);
		eventBundle.putBoolean(Constants.IsSavedEventFlag, true);
		eventBundle.putDouble(Constants.CurrentLatitude,
				currentLocation.getLatitude());
		eventBundle.putDouble(Constants.CurrentLongitude,
				currentLocation.getLongitude());
		return eventBundle;
	}

	public void errorOccurred() {
		new AlertDialog.Builder(this)
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

	public void setLocations(List<Place> pubPlaces) {
		this.pubPlaces = pubPlaces;		
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
		private boolean locationSet = false;
		private boolean peopleFound = false;
		private boolean pubFound = false;
		
		public void onLocationChanged(Location location) //we get the location
		{
			locationSet = true;
			
			//Start tasks: Get people & get pubs
			PersonFinder personFinder = new PersonFinder(service);
			Pending.this.updateText("Finding friends");
			pubFound = true;
			
			personFinder.getFriends(new IRequestListener<AppUserArray>() {
				@Override
				public void onRequestComplete(AppUserArray data) {
					peopleFound = true;
					allFriends = data.getArray();
					if(pubFound)
					{
						//Start next batch of requests
						createEvent();
						PersonRanker p = new PersonRanker(event, service, allFriends, new IRequestListener<PubEvent>() {

							@Override
							public void onRequestComplete(PubEvent data) {
								Pending.this.finish();
							}

							@Override
							public void onRequestFail(Exception e) {
								// TODO Auto-generated method stub
								
							}
							
						});
					}
					else
					{
						Pending.this.updateText("Finding pubs");
					}
				}

				@Override
				public void onRequestFail(Exception e) {
					Pending.this.errorOccurred();
				}
				
			});
			
			//TODO: Call PubFinder 
			//PubFinding pubFinding = new PubFinding(serivce);
			/*
			 * 
			 */
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
}

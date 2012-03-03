package dimappers.android.pub;

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

	@Override
	public void onStart() {
		super.onStart();
		if (firstTime) {
			firstTime = false;
			findLocation();
		}
	}

	private void findLocation() {
		updateText("Finding current location");
		LocationFinder lc = new LocationFinder(this);
		currentLocation = lc.findLocation();
		if(currentLocation!=null) {continueGoing();}
	}
	public void continueGoing() {
		if(serviceConnected) {startTasks();}
		locationFound = true;
	}
	public void updateText(String s) {
		text.setText(s);
	}

	public void startTasks() {

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

		Object[] info2 = new Object[2];
		info2[0] = this;
		info2[1] = service;
		
		new PersonFinder().execute(info2);
	}

	public void createEvent() {
		updateText("Creating Event");

		Bundle b = getIntent().getExtras();
		if (b == null) {
			Debug.waitForDebugger();
		}

		facebookUser = (User) b
				.getSerializable(Constants.CurrentFacebookUser);
		event = new PubEvent(new TimeFinder().chooseTime(), new User(
				facebookUser.getUserId()));
	}

	public void onClick(View v) {
		if (v.getId() == R.id.cancelbutton) {finish();}
	}

	public void onFinish() {
		event = new PersonRanker(event, service).getEvent();
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
			if(locationFound) {startTasks();}
			serviceConnected = true;
		}

		public void onServiceDisconnected(ComponentName className)
		{
		}
		
	};
}

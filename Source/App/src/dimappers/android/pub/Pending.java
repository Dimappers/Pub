package dimappers.android.pub;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class Pending extends Activity implements OnClickListener {

	private TextView progressText;
	private LocationManager locationManager;
	IPubService service;
	PersonRanker personRanker;
	
	long timestarted = 0;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pending_guests);
		
		timestarted = System.currentTimeMillis();
		
		//Bind to service
    	bindService(new Intent(this, PubService.class), connection, 0);
    	Typeface font = Typeface.createFromAsset(getAssets(), "SkratchedUpOne.ttf");
		progressText = (TextView) findViewById(R.id.location_error);
		((TextView)findViewById(R.id.location_error)).setTypeface(font);
		((TextView) findViewById(R.id.cancelbutton)).setOnClickListener(this);
	}

	
	private void findLocation() {
		updateText("Finding current location");
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationFinder lc = new LocationFinder(locationManager);
		
		//Find the users current location - required for all other tasks
		lc.findLocation(locationListener);
		
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			
			
			public void run() {
				if(!locationListener.locationFound())
				{
					locationManager.removeUpdates(locationListener);
					final PersonFinder personFinder = new PersonFinder(service, getApplicationContext());
					Pending.this.updateText("Finding friends");
					runOnUiThread(new Runnable()
					{
						
						public void run()
						{
							personFinder.getFriends(new IRequestListener<AppUserArray>() {
							
							public void onRequestComplete(final AppUserArray data) {
								
										locationListener.peopleFound = true;
										locationListener.allFriends = data.getArray();
										if(locationListener.pubFound)
										{
											locationListener.rankThings(locationListener.pubs);
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
					});
				
					
					changeLocation("Could not find location");
				}
			}
		}, 1000 * 10); //wait 10 seconds
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
		return new PubEvent(new TimeFinder(service.getHistoryStore()).chooseTime(), service.GetActiveUser());
	}

	
	public void onClick(View v) {
		if (v.getId() == R.id.cancelbutton) {finish();}
	}

	public void onFinish(PubEvent createdEvent) {
		setResult(Activity.RESULT_OK, new Intent().putExtras(fillBundle(createdEvent)));
		finish();
	}
	
	
	public void onDestroy()
	{
		super.onDestroy();
		if(locationManager!=null&&locationListener!=null) {locationManager.removeUpdates(locationListener);}
		unbindService(connection);
		if(personRanker!=null&&personRanker.t!=null){personRanker.t.cancel();}
		
		Log.d(Constants.MsgInfo, "Time taken getting through pending is: " + (System.currentTimeMillis() - timestarted) +".");
	}

	private Bundle fillBundle(PubEvent createdEvent) {
		service.GiveNewSavedEvent(createdEvent);
		Bundle eventBundle = new Bundle();
		eventBundle.putInt(Constants.CurrentWorkingEvent, createdEvent.GetEventId());
		eventBundle.putBoolean(Constants.IsSavedEventFlag, false);
		return eventBundle;
	}

	public void errorOccurred() {
		runOnUiThread(new Runnable(){

			
			public void run() {
				Toast.makeText(getApplicationContext(), "Unexpected error occurred: recommendations may not be accurate.", Toast.LENGTH_LONG).show();
			}});
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
	
	private class OurLocationListener implements LocationListener
	{
		private boolean peopleFound = false;
		private boolean pubFound = false;
		
		private List<Place> pubs = null;
		private AppUser[] allFriends = null;
		
		public Location currentLocation = null;
		
		public boolean locationFound()
		{
			return currentLocation != null;
		}
		
		
		public void onLocationChanged(Location location) //we get the location
		{
			locationManager.removeUpdates(this);
			currentLocation = location;
			
			double[] locationarray = new double[2];
			locationarray[0] = location.getLatitude();
			locationarray[1] = location.getLongitude();
			service.GetActiveUser().setLocation(locationarray);
			
			//Start tasks: Get people & get pubs
			PersonFinder personFinder = new PersonFinder(service, getApplicationContext());
			Pending.this.updateText("Finding friends");
			
			findPubs();
			
			
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
		private void findPubs()
		{
			DataRequestPubFinder pubFinder = new DataRequestPubFinder(currentLocation.getLatitude(), currentLocation.getLongitude());
			service.addDataRequest(pubFinder, new IRequestListener<PlacesList>(){

				
				public void onRequestComplete(PlacesList data) {
					if(data.status.equals("ZERO_RESULTS"))
					{
						changeLocation("Could not find any pubs at this location");
					}
					else
					{
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
				}

				
				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, e.getMessage());
					Pending.this.errorOccurred();
				}});
		}
		
		private void rankThings(final List<Place> pubs)
		{
			//Start next batch of requests
			PubEvent event = createEvent();
			personRanker = new PersonRanker(event, Pending.this, currentLocation, allFriends, new IRequestListener<PubEvent>() {

				
				public void onRequestComplete(PubEvent data) {
					data.SetPubLocation(new PubRanker(pubs, data, Pending.this.service.getHistoryStore()).returnBest());
					Pending.this.onFinish(data);
				}

				
				public void onRequestFail(Exception e) {
					errorOccurred();
				}
				
			}, getContentResolver());
		}

		
		public void onProviderDisabled(String provider) {
			Log.d(Constants.MsgWarning, provider + " is not avaliable");
			Pending.this.errorOccurred();
		}

		
		public void onProviderEnabled(String provider) {
			Log.d(Constants.MsgInfo, provider + " is avaliable");
		}

		
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if(status==LocationProvider.OUT_OF_SERVICE) //i.e. location will not be able to be provided for a while
			{
				Pending.this.errorOccurred();
			}
		}
	}
	
	private OurLocationListener locationListener = new OurLocationListener();

	private void changeLocation(final String message)
	{
		runOnUiThread(new Runnable()
		{
			
			
			public void run()
			{
				final EditText loc = new EditText(getApplicationContext());
				new AlertDialog.Builder(Pending.this).setMessage("Enter a different location:")  
				.setTitle(message)
				.setCancelable(true)  
				.setPositiveButton("Use this location", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int id) {
						
						DataRequestReverseGeocoder reverseGeocoder = new DataRequestReverseGeocoder(getApplicationContext(), loc.getText().toString());
						service.addDataRequest(reverseGeocoder, new IRequestListener<XmlableDoubleArray>(){
	
							
							public void onRequestFail(Exception e) {
								Pending.this.errorOccurred();
							}
	
							
							public void onRequestComplete(XmlableDoubleArray data) {
								if(locationListener.currentLocation == null)
								{
									locationListener.currentLocation = new Location("UserEntered");
								}
								locationListener.currentLocation.setLatitude(data.getArray()[0]);
								locationListener.currentLocation.setLongitude(data.getArray()[1]);
								service.GetActiveUser().setLocation(data.getArray());
								locationListener.findPubs();
								
							}});
						dialog.cancel();
					}
				})
				.setView(loc)
				.show(); 
			}});
	}
}

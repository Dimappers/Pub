package dimappers.android.pub;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class Pending extends Activity {
	View v;
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pending_guests);
    	findViewById(R.id.location_error).setVisibility(View.INVISIBLE);
    	//TODO: find suitable guests etc.
    	findLocation();
    	
		
	}	
	
	//Finding current location
	private void findLocation()
	{		
		//Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		//Define a listener that responds to location updates
		MyLocationListener locationListener = new MyLocationListener(this);
		//Using most recent location before searching to allow for faster loading
		
		//TO EMULATOR USERS: Must use DDMS (Window>Perspective) to set a GPS location before clicking organise
		Location location = (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
		if(location != null)
		{
			GiveLocation(location);
		}
		else
		{
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		}
		/*TODO: Not sure if we even need this bottom bit - could just use the last known location. 
		 * In that case MyLocationListener could be incorporated into this class*/
		
		//Register the listener with the Location Manager to receive location updates
		
	 }
	
	public void GiveLocation(Location location)
	{
		DoLoading task = new DoLoading();
		task.SetLocation(location);
		task.execute(this);
	}
}


class DoLoading extends AsyncTask<Pending,Integer,Integer>
{
	private PubEvent event;
	private Pending activity;
	private AppUser facebookUser;
	private Location location;
	@Override
	protected Integer doInBackground(Pending... params) {
        if(location == null)
        {
        	Log.d(Constants.MsgError, "Set location first!!");
        }
        else
        {
        	Log.d(Constants.MsgInfo, "Using location: " + location.getLatitude() + ", " + location.getLongitude());
        }
		
		
		activity = params[0];
        
        Bundle b = activity.getIntent().getExtras();
        if(b == null)
        {
        	Debug.waitForDebugger();        	
        }
        facebookUser = (AppUser)b.getSerializable(Constants.CurrentFacebookUser);
    	//Toast.makeText(activity.getApplicationContext(), "Received id: " + new Integer(facebookId).toString(), Toast.LENGTH_LONG).show();
    	event = new PubEvent(Calendar.getInstance(), facebookUser);
    	event.SetPubLocation(new PubLocation());
    	event.AddUser(new AppUser(143));
    	event.AddUser(new AppUser(12341));
        
		return null;
	}
	protected void onPostExecute(Integer result) {
		//TODO: pass updated event back
		Bundle eventBundle = new Bundle();
		eventBundle.putAll(activity.getIntent().getExtras());
		eventBundle.putSerializable(Constants.CurrentWorkingEvent, event);
		eventBundle.putBoolean(Constants.IsSavedEventFlag, true);
		eventBundle.putDouble(Constants.CurrentLatitude, location.getLatitude());
		eventBundle.putDouble(Constants.CurrentLongitude, location.getLongitude());
		
		Intent intent = new Intent();
		intent.putExtras(eventBundle);
		
		activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }
	
	public void SetLocation(Location location)
	{
		this.location = location;
	}
}

class MyLocationListener implements LocationListener{
	Pending pending;
	MyLocationListener(Pending pending) {
		this.pending = pending; 
	}
	public void onLocationChanged(Location location) {makeUseOfNewLocation(location);}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
	
	//This method should find the current town from the latitude/longitude of the location
	public void makeUseOfNewLocation(Location location) {
		pending.GiveLocation(location);
	}
}

package dimappers.android.pub;

import java.util.Calendar;
import java.util.List;

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
import android.widget.TextView;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class Pending extends Activity {
	TextView text;
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pending_guests);
    	text = (TextView) findViewById(R.id.location_error);
    	findLocation();	
	}	
	
	//Finding current location
	private void findLocation()
	{		
		updateText("Finding current location");
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
	public void updateText(String s) {
		text.setText(s);
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
        publishProgress(new Integer(0));
    	event = new PubEvent(Calendar.getInstance(), facebookUser);
    	publishProgress(new Integer(1));
    	findPub();
    	publishProgress(new Integer(2));
    	event.AddUser(new AppUser(143));
    	event.AddUser(new AppUser(12341));
        
		return null;
	}
	protected void onProgressUpdate(Integer... progress) {
		if(progress[0].intValue()==0) {activity.updateText("Creating new event");}
		else if(progress[0].intValue()==1) {activity.updateText("Choosing a pub");}
		else if(progress[0].intValue()==2) {activity.updateText("Picking guests");}
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
	private void findPub() {
		PubFinder pubfinder = new PubFinder(location.getLatitude(),location.getLongitude());
		Place pub = new Place();
		pub.name="Unknown";
		double lat = 0;
		double lng = 0;
		try {
			List<Place> list = pubfinder.performSearch();
			if(list!=null&&list.size()!=0)
			{
				pub = list.get(0);
				lat = pub.geometry.location.lat;
				lng = pub.geometry.location.lng;
			}
		} catch (Exception e) {
			Log.d(Constants.MsgError,"Error while finding pubs.");
			e.printStackTrace();
		}
		event.SetPubLocation(new PubLocation(lat,lng,pub.name));
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

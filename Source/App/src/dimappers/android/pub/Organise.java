package dimappers.android.pub;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Organise extends Activity implements OnClickListener{
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.organise);
	    	
	    	TextView cur_loc = (TextView)findViewById(R.id.current_location);
	    	TextView cur_pub = (TextView)findViewById(R.id.current_pub);
	    	
	    	//new FindLocation().execute(this); this bit makes stuff crash
	    	
	    	Button button_organise = (Button)findViewById(R.id.location_button);
	    	button_organise.setOnClickListener(this);
	    	Button button_choose_guests = (Button)findViewById(R.id.chosen_guests_button);
	    	button_choose_guests.setOnClickListener(this);
	    	Button button_choose_time = (Button)findViewById(R.id.time_button);
	    	button_choose_time.setOnClickListener(this);
	    	Button button_save_event = (Button)findViewById(R.id.save_event);
	    	button_save_event.setOnClickListener(this);
	    	Button button_send_invites = (Button)findViewById(R.id.send_invites_event);
	    	button_send_invites.setOnClickListener(this);
	 }
	 public void onClick(View v)
	 {
		 Intent i;
		 switch (v.getId()){
			case R.id.location_button : {
				i = new Intent(this, ChoosePub.class);
				startActivity(i);
				break;
			}
			case R.id.chosen_guests_button : {
				i = new Intent(this, Pending.class);
				startActivityForResult(i, 0);
				break;
			}
			case R.id.time_button : {
				i = new Intent(this, ChooseTime.class);
				startActivity(i);
				break;
			}
			case R.id.save_event : {
				//TODO: save event details
				//May want this to end this activity & then go to Events.class,
				//so can't go back to it by clicking back button
				this.setResult(RESULT_OK, getIntent());
				finish();
				break;
			}
			case R.id.send_invites_event : {
				//TODO: save event details, then send invites to server
				//May want this to end this activity & then go to Events.class,
				//so can't go back to it by clicking back button
				this.setResult(RESULT_OK, getIntent());
				finish();
				break;
			}
		 }
	 }
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if(requestCode==0)
		 {
			 super.onActivityResult(requestCode, resultCode, data);
			 Intent i = new Intent(this, Guests.class);	
			 startActivity(i);
		 }
	 }
}

//FIXME:the location is found (on some phones) but when a town is attempted to be found from this, the app crashes

class FindLocation extends AsyncTask<Organise,Integer,Integer> {
	private TextView cur_loc;
	private Organise organise;
	@Override
	protected Integer doInBackground(Organise... params) 
	{
		organise = params[0];
		cur_loc = (TextView)organise.findViewById(R.id.current_location);
		
		//Finding current location
		//Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager)organise.getSystemService(Context.LOCATION_SERVICE);
		//Define a listener that responds to location updates
		MyLocationListener locationListener = new MyLocationListener(organise);
		//Using most recent location before searching to allow for faster loading
		Location location = (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		cur_loc.setText(location.getLatitude() + " lat & long " + location.getLongitude());
		//locationListener.makeUseOfNewLocation(location); <<if this is included then the app always crashes
		//Register the listener with the Location Manager to receive location updates
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		
		return null;
	 }
}

class MyLocationListener implements LocationListener {
	Organise organise;
	TextView cur_loc;
	MyLocationListener(Organise organise) {
		this.organise = organise; 
		cur_loc = (TextView)organise.findViewById(R.id.current_location);
	}
	public void onLocationChanged(Location location) {
		//Called when a new location is found by the network location provider.
		makeUseOfNewLocation(location);
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
	
	//This method should find the current town from the latitude/longitude of the location
	public void makeUseOfNewLocation(Location location) {
		if(location!=null)
		{
			Geocoder gc = new Geocoder(organise.getApplicationContext());
			try {
				List<Address> list = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (list.size() > 0) {
					cur_loc.setText("the list exists now");//list.get(0).getLocality());
				}
			} catch (IOException e) {
				cur_loc.setText(location.getLatitude() + "lat and long" + location.getLongitude());//"Location is unavailable, please manually set pub.");
			}
		}
	}
}

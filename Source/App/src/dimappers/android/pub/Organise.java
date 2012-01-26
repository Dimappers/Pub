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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Organise extends Activity implements OnClickListener{
	
	private TextView cur_loc;
	private TextView cur_pub;
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.organise);
	    	
	    	cur_loc = (TextView)findViewById(R.id.current_location);
	    	cur_pub = (TextView)findViewById(R.id.current_pub);
	    	
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
	    	
	    	AppUser user = new AppUser(12);
	    	
	    	Toast.makeText(getApplicationContext(), "User id:" + user.getUserId(), 5000).show();
	 }
	 @Override
	 public void onStart() {super.onStart(); findLocation();}
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
	//Finding current location
	private void findLocation()
	{		
		//Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		//Define a listener that responds to location updates
		MyLocationListener locationListener = new MyLocationListener(this);
		//Using most recent location before searching to allow for faster loading
		Location location = (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		locationListener.makeUseOfNewLocation(location);
		
		/*TODO: Not sure if we even need this bottom bit - could just use the last known location. 
		 * In that case MyLocationListener could be incorporated into this class*/
		
		//Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	 }
}

class MyLocationListener implements LocationListener{
	Organise organise;
	TextView cur_loc;
	MyLocationListener(Organise organise) {
		this.organise = organise; 
		cur_loc = (TextView)organise.findViewById(R.id.current_location);
	}
	public void onLocationChanged(Location location) {makeUseOfNewLocation(location);}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
	
	//This method should find the current town from the latitude/longitude of the location
	public void makeUseOfNewLocation(Location location) {
		String place = null;
		if(location!=null)
		{
			Geocoder gc = new Geocoder(organise.getApplicationContext());
			try {
				List<Address> list = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
				int i = 0;
				while (i<list.size()) 
				{
					String temp = list.get(i).getLocality();
					if(temp!=null) {place = temp;}
					i++;
				}
				if(place!=null) {cur_loc.setText(place);}
				else {cur_loc.setText("(" + location.getLatitude() + "," + location.getLongitude() + ")");}
			}
			//This is thrown if the phone has no Internet connection.
			catch (IOException e) {
				cur_loc.setText("(" + location.getLatitude() + "," + location.getLongitude() + ")");
			}
		}
	}
}

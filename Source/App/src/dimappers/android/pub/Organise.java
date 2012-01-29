package dimappers.android.pub;

import java.io.IOException;
import java.util.List;
import java.util.Date;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;

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
	private PubEvent event;
	private int facebookId;
	 @Override
	 public void onCreate(Bundle savedInstanceState)
	 {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.organise);
	    	
	    	Bundle b = getIntent().getExtras();
	    	if(b.getSerializable("event")!=null)
	    	{
	    		event=(PubEvent)b.getSerializable("event");
	    		Toast.makeText(getApplicationContext(), "Received event: " + event.GetHost().getUserId().toString(), Toast.LENGTH_LONG).show();
	    	}
	    	else{
		    	facebookId = b.getInt("facebookId");
		    	Toast.makeText(getApplicationContext(), "Received id: " + new Integer(facebookId).toString(), Toast.LENGTH_LONG).show();
		    	Date date = new Date();
		    	Integer fb = new Integer(facebookId);
		    	AppUser host = new AppUser(fb);
		    	event = new PubEvent(date, (User)host);
	    	}

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
	 }
	 @Override
	 public void onStart(){
		 super.onStart(); 
		 findLocation();
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
				startActivityForResult(i, 1);
				break;
			}
			case R.id.time_button : {
				i = new Intent(this, ChooseTime.class);
				Bundle b = new Bundle();
				b.putSerializable("event", event);
				i.putExtras(b);
				startActivityForResult(i,3);
				break;
			}
			case R.id.save_event : {
				//TODO: save event details
				this.setResult(RESULT_OK, getIntent());
				finish();
				break;
			}
			case R.id.send_invites_event : {
				//TODO: save event details, then send invites to server
				this.setResult(RESULT_OK, getIntent());
				finish();
				break;
			}
		 }
	 }
	 @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 if(resultCode==RESULT_OK) //This line is so when the back button is pressed the data changed by an Activity isn't stored.
		 {
			 if(requestCode==1)
			 {
				 Intent i = new Intent(this, Guests.class);	
				 startActivity(i);
			 }
			 if(requestCode==3)
			 {
				 Date startTime = (Date)data.getExtras().getSerializable("time");
				 Toast.makeText(getApplicationContext(), "received info from ChooseTime: " + startTime.toString(), Toast.LENGTH_LONG).show();
			 } 
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

package dimappers.android.pub;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;

import android.app.Activity;
import android.app.ListActivity;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Organise extends ListActivity implements OnClickListener{
	
	private Button cur_pub;
	private Button cur_time;
	
	private PubEvent event;
	private int facebookId;
	
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private ListView guest_list;
	
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
	    		
	    		if(b.getBoolean("NewEvent"))
	    		{
	    			Toast.makeText(getApplicationContext(), "New event...", 100).show();	    			
	    		}
	    		else
	    		{
	    			Toast.makeText(getApplicationContext(), "Old event...", 100).show();
	    		}
	    	}
	    	else{
	    		//TODO: this needs changing when pending passes in an event
		    	int i =  1/0;
	    	} 

	    	cur_pub = (Button)findViewById(R.id.pub_button);
	    	cur_time = (Button)findViewById(R.id.time_button);
	    	
	    	guest_list = (ListView)findViewById(android.R.id.list);
	    	adapter = new ArrayAdapter<String>(this, android.R.layout.test_list_item, listItems);
	    	
	    	setListAdapter(adapter);
	    	
	    	UpdateFromEvent();
	    	
	    	cur_pub.setOnClickListener(this);
	    	
	    	guest_list.setOnItemClickListener(new OnItemClickListener() {
	    	    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
	    	    	Intent i = new Intent(getBaseContext(), Guests.class);
	    	    	Bundle b = new Bundle();
	    	    	b.putSerializable("event", event);
	    	    	i.putExtras(b);
					startActivityForResult(i, 4);
	    	        }
	    	      });
	    	
	    	
	    	cur_time.setOnClickListener(this);
	    	
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
	 public void onListItemClick(View v)
	 {
		if(v.getId()==android.R.id.list) {
			Intent i = new Intent(this, Guests.class);
			Bundle b = new Bundle();
			b.putSerializable("event",event);
			i.putExtras(b);
			startActivity(i);
		}
	 }
	 public void onClick(View v)
	 {
		Intent i;
		Bundle b = new Bundle();
		b.putSerializable("event", event);
		 switch (v.getId()){
			case R.id.pub_button : {
				i = new Intent(this, ChoosePub.class);
				i.putExtras(b);
				startActivityForResult(i,1);
				break;
			}
			case R.id.time_button : {
				i = new Intent(this, ChooseTime.class);
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
		 //super.onActivityResult(requestCode, resultCode, data);
		 if(resultCode==RESULT_OK) //This line is so when the back button is pressed the data changed by an Activity isn't stored.
		 {
			 if(requestCode==1)
			 {
				 event = (PubEvent)data.getExtras().getSerializable("eventt");
				 Toast.makeText(getApplicationContext(), event.GetPubLocation().pubName, Toast.LENGTH_LONG).show();
				 UpdateFromEvent();
			 }
			 if(requestCode==3)
			 {
				 event = (PubEvent)data.getExtras().getSerializable("event");
				 UpdateFromEvent();
			 } 
			 else if(requestCode == 4)
			 {
				 event = (PubEvent)data.getExtras().getSerializable("event");
				 UpdateFromEvent();
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
	
	private void UpdateFromEvent()
	{
		cur_pub.setText(event.GetPubLocation().pubName);
		cur_time.setText(event.GetStartTime().getTime().toString());
		
		listItems.clear();
    	for(User s : event.GetUsers()) {
    		listItems.add(((AppUser) s).GetRealFacebookName());
    	}
    	
    	adapter.notifyDataSetChanged();
	}
	
	private void sendEventToServer() throws UnknownHostException, IOException {
		//TODO: Open a socket
		
		Socket socket = null; 
		
		socket = new Socket(Constants.ServerIp, Constants.Port);
		
		ObjectOutputStream serializer = null;
		
		serializer = new ObjectOutputStream(socket.getOutputStream());
		//TODO: Send a message to server for new Event
		
		MessageType t = MessageType.newPubEventMessage;
		serializer.writeObject(t);
		serializer.writeObject(event);
		serializer.flush();
		//TODO: Send the event
		//TODO: Send to Pending Screen
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

package dimappers.android.pub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;

public class Organise extends ListActivity implements OnClickListener, OnMenuItemClickListener{

	private Button cur_pub;
	private Button cur_time;
	private TextView cur_loc;
	private ProgressBar progbar;
	MenuItem edit;

	private PubEvent event;
	
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private ListView guest_list;

	private boolean locSet = false;
	private boolean eventSavedAlready;
	private double latSet;
	private double lngSet;

	private AppUser[] facebookFriends;

	IPubService service;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.organise);

		//Bind to service
		bindService(new Intent(this, PubService.class), connection, 0);

		//TODO: If we are editing may need to modify user interface
		eventSavedAlready = getIntent().getExtras().getBoolean(Constants.IsSavedEventFlag);
		if(eventSavedAlready)
		{
			Log.d(Constants.MsgInfo, "Event has been created before");	    			
		}
		else
		{
			Log.d(Constants.MsgInfo, "Event has just been generated");
		}
		
		Typeface font = Typeface.createFromAsset(getAssets(), "SkratchedUpOne.ttf");

		//Always available buttons
		cur_pub = (Button)findViewById(R.id.pub_button);
		cur_time = (Button)findViewById(R.id.time_button);
		cur_loc=(TextView)findViewById(R.id.current_location);
    	cur_pub.setTypeface(font);
    	cur_time.setTypeface(font);
    	cur_loc.setTypeface(font);
		cur_pub.setOnClickListener(this);
		cur_time.setOnClickListener(this);
		cur_loc.setOnClickListener(this);
		
		//Always visible text
    	((TextView)findViewById(R.id.time_title)).setTypeface(font);
    	((TextView)findViewById(R.id.pub_title)).setTypeface(font);
    	((TextView)findViewById(R.id.guest_title)).setTypeface(font);
    	
    	//Progress bar
		progbar = (ProgressBar)findViewById(R.id.progressBar);

		//Guest list
		guest_list = (ListView)findViewById(android.R.id.list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.test_list_item, listItems);
		setListAdapter(adapter);

	}
	
	class TextUpdater implements Runnable {

		String s;
		
		TextUpdater(String s) {this.s = s;}
		
		public void run() {
			cur_loc.setText(s);
		}
		
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}

	void packageLocForActivity(double[] loc, Bundle b, Intent i)
	{
		b.putDouble(Constants.CurrentLatitude, loc[0]);
		b.putDouble(Constants.CurrentLongitude, loc[1]);
		i.putExtras(b);
		startActivityForResult(i, Constants.PubLocationReturn);
	}
	
	public void onClick(View v)
	{
		final Intent i;
		final Bundle b = new Bundle();
		b.putAll(getIntent().getExtras()); 
		b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
		switch (v.getId()){
		case R.id.current_location : {
			setLocation();
		}
			case R.id.pub_button : {
				i = new Intent(this, ChoosePub.class);
				if(locSet){
					b.putDouble(Constants.CurrentLatitude, latSet);
					b.putDouble(Constants.CurrentLongitude, lngSet);
					i.putExtras(b);
					startActivityForResult(i, Constants.PubLocationReturn);
				}
				else if (service!=null)
				{
					double[] loc = service.GetActiveUser().getLocation();

					if(loc!=null)
					{
						packageLocForActivity(loc, b, i);
					}
					else
					{
					final LocationManager locMan = (LocationManager)getSystemService(LOCATION_SERVICE);
					new LocationFinder(locMan).findLocation(new LocationListener(){

						public void onLocationChanged(Location arg0) {
							double[] location = new double[2];
							location[0] = arg0.getLatitude();
							location[1] = arg0.getLongitude();
							service.GetActiveUser().setLocation(location);
							locMan.removeUpdates(this);
							packageLocForActivity(location, b, i);
						}
						public void onProviderDisabled(String provider) {
							runOnUiThread(new Runnable(){

								public void run() {
									Toast.makeText(getApplicationContext(), "Cannot find your current location.", Toast.LENGTH_LONG).show();
								}});
						}
						public void onProviderEnabled(String provider) {}
						public void onStatusChanged(String provider,int status, Bundle extras) {}});
					}
				}
				break;
			}
			case R.id.time_button : {
				i = new Intent(this, ChooseTime.class);
				i.putExtras(b);
				startActivityForResult(i,Constants.StartingTimeReturn);
				break;
			}
			case R.id.save_event : {
				if(!eventSavedAlready)
				{
					//TODO: This may not be in the correct position, kind of needs issue 120
					DataRequestUpdateEvent updateRequest = new DataRequestUpdateEvent(event);
					service.addDataRequest(updateRequest, new IRequestListener<PubEvent>(){

						
						public void onRequestComplete(PubEvent data) {
							if(data != null)
							{
								event = data;
								UpdateFromEvent();
							}
						}

						
						public void onRequestFail(Exception e) {
							// TODO Auto-generated method stub
							
						}
						
					});
				}
				i = new Intent();
				service.GiveNewSavedEvent(event);
				b.putBoolean(Constants.IsSavedEventFlag, true);
				i.putExtras(b);
				setResult(RESULT_OK, i);
				finish();
				break;
			}
			case R.id.send_invites_event : {
				service.GiveNewSentEvent(event, new IRequestListener<PubEvent>() {
					
					public void onRequestFail(Exception e) {
						Log.d(Constants.MsgError, "Could not send event");
						e.printStackTrace();
						runOnUiThread(new Runnable(){

							public void run() {
								progbar.setVisibility(View.GONE);
								Toast.makeText(getApplicationContext(),"Unable to send event, please try again later.",Toast.LENGTH_LONG).show();
								//FIXME: probably should make it more obvious when this fails
							}});
					}
					
					public void onRequestComplete(PubEvent data) {
							onSendSuccess(data);
					}
				});
				
				
				// Inflating the loading bar
				/*LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
				ViewGroup parent = (ViewGroup) findViewById(R.id.organise_screen);

				View pBar = inflater.inflate(R.layout.loading_bar, parent, false);
				parent.addView(pBar);*/
				progbar.setVisibility(View.VISIBLE);
				break;
			}
		}
	}
	
	private void onSendSuccess(PubEvent data)
	{
		Log.d(Constants.MsgInfo, "PubEvent sent, event id: " + data.GetEventId());
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putAll(getIntent().getExtras()); 
		b.putSerializable(Constants.CurrentWorkingEvent, data.GetEventId());
		b.putBoolean(Constants.IsSavedEventFlag, false);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK) //This line is so when the back button is pressed the data changed by an Activity isn't stored.
		{ 
			//We don't actually care what we are returning from, always get the latest event and update the screen
			event = service.getEvent(data.getExtras().getInt(Constants.CurrentWorkingEvent));
			UpdateFromEvent();
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		edit = menu.add(0, Menu.NONE, 0, "Change Location");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(service!=null)
		{
			edit.setOnMenuItemClickListener(Organise.this); 
			//don't let people change the location before we have connected to the service, as we need the service to find pubs etc. from current location
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onMenuItemClick(MenuItem item) {
		switch(item.getItemId()){
			case Menu.NONE : {
				setLocation();
				return true;
			}
		}
		return false;
	}
	
	void setLocation()
	{
		final EditText loc = new EditText(getApplicationContext());
		new AlertDialog.Builder(this).setMessage("Enter your current location:")  
		.setTitle("Change Location")  
		.setCancelable(true)  
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				
				progbar.setVisibility(View.VISIBLE);
				
				DataRequestReverseGeocoder request1 = new DataRequestReverseGeocoder(getApplicationContext(), loc.getText().toString());
				service.addDataRequest(request1, new IRequestListener<XmlableDoubleArray>(){

					public void onRequestFail(Exception e) {
						failure(2);
					}

					public void onRequestComplete(XmlableDoubleArray data) {
						
						final double lat = data.getArray()[0];
						final double lng = data.getArray()[1];
						
						DataRequestPubFinder request2 = new DataRequestPubFinder(lat, lng);
						service.addDataRequest(request2, new IRequestListener<PlacesList>(){

							public void onRequestComplete(PlacesList data) {
								PubLocation best = new PubRanker(data.results, event, service.getHistoryStore()).returnBest();
								if(best==null) {failure(0);}
								else
								{
									event.SetPubLocation(best);
									success(lat, lng, loc.getText().toString());
								}
								}

							public void onRequestFail(Exception e) {
								failure(1);
							}});
					}});
				dialog.cancel();
			}
		})
		.setView(loc)
		.show(); 
	}
	
	void success(double lat, double lng, final String loc)
	{
		latSet=lat;
		lngSet=lng;
		locSet=true; 
		runOnUiThread(new Runnable(){
			public void run() {
				cur_loc.setText(loc);
				UpdateFromEvent();
				removeProgBar();
			}});
	}
	void failure(int which)
	{
		removeProgBar();
		Log.d(Constants.MsgError, "Error using custom location!!");
		if(which==0) //no pubs found
		{
			runOnUiThread(new Runnable(){
				public void run() {
					Toast.makeText(getApplicationContext(), "No pubs found near this location", Toast.LENGTH_SHORT).show();
				}});
		}
		else if(which==1) //error finding pubs
		{
			runOnUiThread(new Runnable(){
			public void run() {
				Toast.makeText(getApplicationContext(), "Pubs unable to be found", Toast.LENGTH_SHORT).show();
			}});
		}
		else if(which==2) //error when geocoding
		{
			runOnUiThread(new Runnable(){
				public void run() {
					Toast.makeText(getApplicationContext(), "Unrecognised location", Toast.LENGTH_SHORT).show();
				}});
		}
		else //this shouldn't happen
		{
			runOnUiThread(new Runnable(){
				public void run() {
					Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
				}});
		}
	}
	
	void removeProgBar()
	{
		runOnUiThread(new Runnable(){

			public void run() {
				progbar.setVisibility(View.GONE);
			}});
	}


	private void UpdateFromEvent()
	{
		cur_pub.setText(event.GetPubLocation().getName());
		cur_time.setText(event.GetFormattedStartTime());

		listItems.clear();
		adapter.notifyDataSetChanged();
		for(User user : event.GetUsers()) {
			if(user instanceof AppUser)
			{
				listItems.add(user.toString());
				adapter.notifyDataSetChanged();
			}
			else
			{
				DataRequestGetFacebookUser request = new DataRequestGetFacebookUser(user.getUserId());
				service.addDataRequest(request, new IRequestListener<AppUser>() {

					public void onRequestComplete(final AppUser data) {
						Organise.this.runOnUiThread(new Runnable() {
							public void run() {
								listItems.add(data.toString());
								adapter.notifyDataSetChanged();					
							}
						});
					}

					public void onRequestFail(Exception e) {
						// TODO Auto-generated method stub
						Log.d(Constants.MsgError, e.getMessage());
					}
					
				} );
			}
		}
	}

	//TODO: Move this into the service
	/*private void sendEventToServer() {
		Bundle b = getIntent().getExtras();
		b.putSerializable(Constants.CurrentWorkingEvent, event);

		// Inflating the loading bar
		LayoutInflater i = (LayoutInflater) getLayoutInflater();
		ViewGroup parent = (ViewGroup) findViewById(R.id.organise_screen);

		View pBar = i.inflate(R.layout.loading_bar, parent, false);
		parent.addView(pBar);
		new SendData().execute(this);


	}*/

	private ServiceConnection connection = new ServiceConnection()
	{

		public void onServiceConnected(ComponentName className, IBinder service)
		{
			//Give the interface to the app
			Organise.this.service = (IPubService)service;
			event=Organise.this.service.getEvent(getIntent().getExtras().getInt(Constants.CurrentWorkingEvent));
			
			
			guest_list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					Intent i = new Intent(getBaseContext(), Guests.class);
					Bundle b = new Bundle();
					b.putAll(getIntent().getExtras());
					b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
					i.putExtras(b);
					startActivityForResult(i, Constants.GuestReturn);
				}
			});

			//Get user's current location
			double[] location = Organise.this.service.GetActiveUser().getLocation();
			if(location==null)
			{
				final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				LocationFinder lc = new LocationFinder(locationManager);
				lc.findLocation(new LocationListener(){

					public void onLocationChanged(Location loc) {
						locationManager.removeUpdates(this);
						geocode(loc.getLatitude(), loc.getLongitude());
					}

					public void onProviderDisabled(String arg0) {
						Log.d(Constants.MsgError, arg0 + " is disabled.");
					}

					public void onProviderEnabled(String arg0) {}

					public void onStatusChanged(String arg0, int arg1,Bundle arg2) {}});
			}
			else
			{
				geocode(location[0], location[1]);
			}
						
			//get list of facebook friends
			Organise.this.service.addDataRequest(new DataRequestGetFriends(), new IRequestListener<AppUserArray>(){

				public void onRequestComplete(AppUserArray data) {
					facebookFriends = data.getArray();
				}

				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, "FAIL");
				}});
			

			//update text on save/send buttons depending on whether the event is sent or not
			Button button_save_event = (Button)findViewById(R.id.save_event);
			Button button_send_invites = (Button)findViewById(R.id.send_invites_event);
			
			if(event.GetEventId()<0)
			{
				button_save_event.setOnClickListener(Organise.this);
				button_send_invites.setOnClickListener(Organise.this);
			}
			else
			{
				button_save_event.setVisibility(View.INVISIBLE);
				button_send_invites.setText(R.string.update);
				button_send_invites.setOnClickListener(new OnClickListener(){

					public void onClick(View arg0) {
						progbar.setVisibility(View.VISIBLE);
						DataRequestUpdateEvent update = new DataRequestUpdateEvent(event);
						Organise.this.service.addDataRequest(update, new IRequestListener<PubEvent>(){

							public void onRequestComplete(PubEvent data) {
								if(data==null) {onSendSuccess(event);}
								else {onSendSuccess(data);}
							}

							public void onRequestFail(Exception e) {
								Log.d(Constants.MsgError,"Failed to update event: " + e.getMessage());
								runOnUiThread(new Runnable(){
									public void run() {
										progbar.setVisibility(View.GONE);
										Toast.makeText(getApplicationContext(),"Unable to send event, please try again later.",Toast.LENGTH_LONG).show();
									}});
							}});
					}});
			}
			
			//update screen from event
			UpdateFromEvent();
		}

		public void onServiceDisconnected(ComponentName className)
		{
		}
		
		private void geocode(double latitude, double longitude)
		{			
			//find name of location based on lat/long of user's location
			DataRequestGeocoder geoCoder = new DataRequestGeocoder(latitude, longitude, getApplicationContext());
			Organise.this.service.addDataRequest(geoCoder, new IRequestListener<XmlableString>(){

				public void onRequestComplete(XmlableString data) {
					if(data!=null) {runOnUiThread(new TextUpdater(data.getContents()));}
					else {runOnUiThread(new TextUpdater("Unknown"));}
				}

				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError,"Exception thrown by Geocoder in Organise.");
					e.printStackTrace();
					runOnUiThread(new TextUpdater("Unknown"));
				}});
		}

	};
}

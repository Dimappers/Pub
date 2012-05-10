package dimappers.android.pub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.BaseAdapter;
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
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;

public class Organise extends LocationRequiringActivity implements OnClickListener, OnMenuItemClickListener{

	private Button cur_pub;
	private Button cur_time;
	private TextView cur_loc;
	public ProgressBar progbar;
	MenuItem edit;
	
	private ArrayList<String> listItems=new ArrayList<String>();
	private GuestListAdapter adapter;
	private ListView guest_list;
	
	private String add_guest = "+ ADD GUEST";
	private int posOfLastClicked = -1;

	private boolean locSet = false;
	private boolean eventSavedAlready;
	private double latSet;
	private double lngSet;

	private AppUser[] facebookFriends;
	//private Calendar originalTime;
	boolean changed;
	PubEvent oldEvent = null;

	
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
			changed = false;
		}
		else
		{
			Log.d(Constants.MsgInfo, "Event has just been generated");
			changed = true;
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
		
		findViewById(R.id.editTime).setOnClickListener(this);
		findViewById(R.id.editPubLocation).setOnClickListener(this);
		
		//Always visible text
    	((TextView)findViewById(R.id.time_title)).setTypeface(font);
    	((TextView)findViewById(R.id.pub_title)).setTypeface(font);
    	((TextView)findViewById(R.id.guest_title)).setTypeface(font);
    	((Button)findViewById(R.id.send_invites_event)).setTypeface(font);
    	((Button)findViewById(R.id.save_event)).setTypeface(font);
    	//Progress bar
		progbar = (ProgressBar)findViewById(R.id.progressBar);

		//Guest list
		guest_list = (ListView)findViewById(android.R.id.list);
		adapter = new GuestListAdapter(this,
				R.layout.delete_guest,
				R.id.guestName,
				listItems);
		setListAdapter(adapter);

	}
	
	class GuestListAdapter extends ArrayAdapter<String> {
		public GuestListAdapter(Context context, int layout, int id,  ArrayList<String> list)
		{
			super(context, layout, id, list);
		}
		
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView==null)
			{
				convertView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.delete_guest, null);
			}
			if(position==posOfLastClicked)
			{
				convertView.findViewById(R.id.deleteicon).setVisibility(View.VISIBLE);
				((TextView)convertView.findViewById(R.id.guestName)).setText(listItems.get(position));
				return convertView;
			}
			else
			{
				convertView.findViewById(R.id.deleteicon).setVisibility(View.INVISIBLE);
				((TextView)convertView.findViewById(R.id.guestName)).setText(listItems.get(position));
				return convertView;
			}
		}
	}
	
	class TextUpdater implements Runnable {

		String s;
		
		TextUpdater(String s) {this.s = s;}
		
		public void run() {
			cur_loc.setText(s);
		}
		
	}

	
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
			break;
		}
			case R.id.pub_button :
			case R.id.editPubLocation:
			{
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
			case R.id.time_button : 
			case R.id.editTime: 
			{
				i = new Intent(this, ChooseTime.class);
				b.putBoolean(Constants.HostOrNot, true);
				i.putExtras(b);
				startActivityForResult(i,Constants.StartingTimeReturn);
				break;
			}
			case R.id.save_event : {
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
	
	
	public void onBackPressed() {
	    //Handle the back button
	        //Ask the user if they want to quit
		if(changed)
		{
			if(event.GetEventId() < 0) //is a local event
			{
		        new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Save or discard?")
		        .setMessage("Would you like to save before exiting")
		        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
	
		            
					public void onClick(DialogInterface dialog, int which) {
	
		                service.GiveNewSavedEvent(event);
		                finish();    
		            }
	
		        })
		        .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
					
					
					public void onClick(DialogInterface dialog, int which)
					{
						if(oldEvent == null) //then this is a fresh event
						{
							service.RemoveEventFromStoredDataAndCancelNotification(event);
						}
						else //this is an event being edited, we should revert to last version
						{
							event = new PubEvent(oldEvent.writeXml());
							service.GiveNewSavedEvent(event);
							Intent i = new Intent();
							Bundle b = new Bundle();
							b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
							i.putExtras(b);
							setResult(RESULT_OK, i);
						}
						finish();					
					}
				})
		        .show();
			}
			else
			{
				new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Update or abandon changes?")
		        .setMessage("Would you like to update the event")
		        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
	
		            
					public void onClick(DialogInterface dialog, int which) {
	
		            	//Update the event on the server
		                updateEvent();
		                finish();    
		            }
	
		        })
		        .setNegativeButton("Abandon", new DialogInterface.OnClickListener() {
					
					
					public void onClick(DialogInterface dialog, int which)
					{
						if(oldEvent != null) //abandon changes
						{
							event = new PubEvent(oldEvent.writeXml());
							service.UpdatePubEvent(event);
							Intent i = new Intent();
							Bundle b = new Bundle();
							b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
							i.putExtras(b);
							setResult(RESULT_OK, i);
						}
						else //This should never be true - this is the dialog for a sent event 
						{
							Log.d(Constants.MsgError, "Should always been an old event");
						}
						finish();					
					}
				})
		        .show();
			}
		}
		else
		{
			finish();
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
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK) //This line is so when the back button is pressed the data changed by an Activity isn't stored.
		{ 
			//We don't actually care what we are returning from, always get the latest event and update the screen
			event = service.getEvent(data.getExtras().getInt(Constants.CurrentWorkingEvent));
			UpdateFromEvent();
			changed = true;
		}
	}

	
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		edit = menu.add(0, Menu.NONE, 0, "Change Location");
		return super.onCreateOptionsMenu(menu);
	}
	
	
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
		LocationChanger.changeLocation(this);
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
		listItems.add(add_guest);
		adapter.notifyDataSetChanged();
		for(User user : event.GetUsers()) 
		{
			if(user instanceof AppUser)
			{
				listItems.add(user.toString());
				adapter.notifyDataSetChanged();
			}
			else
			{
				DataRequestGetFacebookUser request = new DataRequestGetFacebookUser(user.getUserId());
				service.addDataRequest(request, new IRequestListener<AppUser>() 
				{
					
					public void onRequestComplete(final AppUser data) 
					{
						Organise.this.runOnUiThread(new Runnable() 
						{
							
							public void run() 
							{
								listItems.add(data.toString());
								adapter.notifyDataSetChanged();					
							}
						});
					}

					
					public void onRequestFail(Exception e) 
					{
						// TODO Auto-generated method stub
						Log.d(Constants.MsgError, e.getMessage());
					}
					
				});
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

	private void updateEvent()
	{
		progbar.setVisibility(View.VISIBLE);
		ArrayList<User> addedUsers = new ArrayList<User>();
		for(User newUser : event.GetUsers())
		{
			if(!oldEvent.GetUsers().contains(newUser))
			{
				addedUsers.add(newUser);
			}
		}
		
		DataRequestUpdateEvent update = new DataRequestUpdateEvent(event, addedUsers);
		Organise.this.service.addDataRequest(update, new IRequestListener<PubEvent>()
		{
			
			public void onRequestComplete(PubEvent data) 
			{
				if(data==null) 
				{
					onSendSuccess(event);
				}
				else 
				{
					onSendSuccess(data);
				}
			}

			
			public void onRequestFail(Exception e) 
			{
				Log.d(Constants.MsgError,"Failed to update event: " + e.getMessage());
				runOnUiThread(new Runnable()
				{
					
					public void run() 
					{
						progbar.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(),"Unable to send event, please try again later.",Toast.LENGTH_LONG).show();
					}
				});
			}
		});
			
				
		//Set the host as up for this time
		UserStatus userStatus = event.GetGoingStatusMap().get(event.GetHost());
		userStatus.freeFrom = event.GetStartTime();
		event.GetGoingStatusMap().put(event.GetHost(), userStatus);
	}
	
	private ServiceConnection connection = new ServiceConnection()
	{

		public void onServiceConnected(ComponentName className, IBinder service)
		{
			//Give the interface to the app
			Organise.this.service = (IPubService)service;
			int eventId = getIntent().getExtras().getInt(Constants.CurrentWorkingEvent);
			event=Organise.this.service.getEvent(eventId);
			if(eventSavedAlready)
			{
				oldEvent = new PubEvent(event.writeXml()); //duplicate the old event
			}
			
			/*originalTime = Calendar.getInstance();
			originalTime.setTimeInMillis(event.GetStartTime().getTimeInMillis());*/
			
			guest_list.setOnItemClickListener(new OnItemClickListener() {
				
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					if(position==0)
					{
						Intent i = new Intent(getBaseContext(), Guests.class);
						Bundle b = new Bundle();
						b.putAll(getIntent().getExtras());
						b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
						i.putExtras(b);
						startActivityForResult(i, Constants.GuestReturn);
						posOfLastClicked = -1;
					}
					else
					{
						String userName = adapter.getItem(position);
						if(posOfLastClicked==position)
						{
							for(User guest : event.GetUsers())
							{
								if(guest.equals(event.GetHost())) {continue;}
								if(guest instanceof AppUser)
								{
									AppUser appGuest = (AppUser)guest;
									if(appGuest.toString().equals(userName))
									{
										event.RemoveUser(guest);
										break;
									}
								}
								else
								{
									Log.d(Constants.MsgWarning, "An \"else\" that should never happen, has happened :'(");
								}
							}
							posOfLastClicked=-1;
						}
						else
						{
							posOfLastClicked = position;
						}
						runOnUiThread(new Runnable()
						{ 
							public void run()
							{
								UpdateFromEvent();
							}
						});
					}
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
			Organise.this.service.addDataRequest(new DataRequestGetFriends(getApplicationContext()), new IRequestListener<AppUserArray>(){

				
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
				
				button_send_invites.setOnClickListener(new OnClickListener()
				{

					
					public void onClick(View arg0) 
					{
						updateEvent();
					}
				});
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
				}
			});
		}
	};
}

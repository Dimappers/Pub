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
import android.location.Address;
import android.location.Geocoder;
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

		cur_pub = (Button)findViewById(R.id.pub_button);
		cur_time = (Button)findViewById(R.id.time_button);
		
		progbar = (ProgressBar)findViewById(R.id.progressBar);

		guest_list = (ListView)findViewById(android.R.id.list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.test_list_item, listItems);

		setListAdapter(adapter);

		cur_pub.setOnClickListener(this);

		guest_list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				
				if(false)
				{
					Intent i = new Intent(getBaseContext(), Guests.class);
					Bundle b = new Bundle();
					b.putAll(getIntent().getExtras());
					b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
					i.putExtras(b);
					startActivityForResult(i, Constants.GuestReturn);
				}
				else 
				{
					String name = (String) parent.getItemAtPosition(position);
					AppUser person = null;
					for(AppUser friend : facebookFriends)
					{
						if(friend.toString().equals(name)) {person = friend; break;}
					}
					if(person==null) {Log.d(Constants.MsgWarning, "friend is not a friend");}
					else
					{
						Intent i = new Intent(getBaseContext(), RankBreakDown.class);
						i.putExtra("person", person);
						i.putExtras(getIntent().getExtras());
						startActivityForResult(i, Constants.GuestReturn);
					}
				}
			}
		});


		cur_time.setOnClickListener(this);

		Button button_save_event = (Button)findViewById(R.id.save_event);
		button_save_event.setOnClickListener(this);
		Button button_send_invites = (Button)findViewById(R.id.send_invites_event);
		button_send_invites.setOnClickListener(this);

		cur_loc=(TextView)findViewById(R.id.current_location);
		cur_loc.setOnClickListener(this);
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

	public void onClick(View v)
	{
		Intent i;
		Bundle b = new Bundle();
		b.putAll(getIntent().getExtras()); 
		b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
		switch (v.getId()){
			case R.id.pub_button : {
				i = new Intent(this, ChoosePub.class);
				if(locSet){
					b.putDouble(Constants.CurrentLatitude, latSet);
					b.putDouble(Constants.CurrentLongitude, lngSet);
				}
				i.putExtras(b);
				startActivityForResult(i, Constants.PubLocationReturn);
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
					}
					
					public void onRequestComplete(PubEvent data) {
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
								failure();
							}

							public void onRequestComplete(XmlableDoubleArray data) {
								
								final double lat = data.getArray()[0];
								final double lng = data.getArray()[1];
								
								DataRequestPubFinder request2 = new DataRequestPubFinder(lat, lng);
								service.addDataRequest(request2, new IRequestListener<PlacesList>(){

									public void onRequestComplete(PlacesList data) {
										PubLocation best = new PubRanker(data.results, event, service.getHistoryStore()).returnBest();
										if(best==null) {failure();}
										else
										{
											event.SetPubLocation(best);
											success(lat, lng, loc.getText().toString());
										}
										}

									public void onRequestFail(Exception e) {
										failure();
									}});
							}});
						dialog.cancel();
					}
				})
				.setView(loc)
				.show(); 
				return true;
			}
		}
		return false;
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
	void failure()
	{
		removeProgBar();
		Log.d(Constants.MsgError, "Error using custom location!!");
		runOnUiThread(new Runnable(){
			public void run() {
				Toast.makeText(getApplicationContext(), "Unrecognised location", Toast.LENGTH_SHORT).show();
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

					public void onRequestComplete(AppUser data) {
						listItems.add(data.toString());
						Organise.this.runOnUiThread(new Runnable() {
							public void run() {
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

			double[] location = Organise.this.service.GetActiveUser().getLocation();
			double latitude = location[0];
			double longitude = location[1];
						
			Organise.this.service.addDataRequest(new DataRequestGetFriends(), new IRequestListener<AppUserArray>(){

				public void onRequestComplete(AppUserArray data) {
					facebookFriends = data.getArray();
				}

				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, "FAIL");
				}});
			
			UpdateFromEvent();
			
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

		public void onServiceDisconnected(ComponentName className)
		{
		}

	};
}

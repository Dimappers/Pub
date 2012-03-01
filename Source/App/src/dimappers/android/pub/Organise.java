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

	private PubEvent event;
	
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private ListView guest_list;

	private boolean locSet = false;
	private double latSet;
	private double lngSet;

	private Facebook facebook;

	IPubService serviceInterface;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.organise);

		//Bind to service
		bindService(new Intent(this, PubService.class), connection, 0);

		Bundle b = getIntent().getExtras();
		if(b.getSerializable(Constants.CurrentWorkingEvent)!=null)
		{
			event=(PubEvent)b.getSerializable(Constants.CurrentWorkingEvent);
			Log.d(Constants.MsgInfo, "Event received - host: " + event.GetHost().getUserId());

			if(b.getBoolean(Constants.IsSavedEventFlag))
			{
				Log.d(Constants.MsgInfo, "Event has been created before");	    			
			}
			else
			{
				Log.d(Constants.MsgInfo, "Event has just been generated");
			}
		}
		else{
			setResult(Constants.MissingDataInBundle);
			finish();
		} 

		cur_pub = (Button)findViewById(R.id.pub_button);
		cur_pub.setText(event.GetPubLocation().toString());
		cur_time = (Button)findViewById(R.id.time_button);
		cur_time.setText(event.GetFormattedStartTime());

		guest_list = (ListView)findViewById(android.R.id.list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.test_list_item, listItems);

		setListAdapter(adapter);

		cur_pub.setOnClickListener(this);

		guest_list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent i = new Intent(getBaseContext(), Guests.class);
				Bundle b = new Bundle();
				b.putAll(getIntent().getExtras());
				b.putSerializable(Constants.CurrentWorkingEvent, event);
				i.putExtras(b);
				startActivityForResult(i, Constants.GuestReturn);
			}
		});


		cur_time.setOnClickListener(this);

		Button button_save_event = (Button)findViewById(R.id.save_event);
		button_save_event.setOnClickListener(this);
		Button button_send_invites = (Button)findViewById(R.id.send_invites_event);
		button_send_invites.setOnClickListener(this);
		cur_loc=(TextView)findViewById(R.id.current_location);
		cur_loc.setOnClickListener(this);

		double latitude = getIntent().getExtras().getDouble(Constants.CurrentLatitude);
		double longitude = getIntent().getExtras().getDouble(Constants.CurrentLongitude);
		String place = null;
		Geocoder gc = new Geocoder(getApplicationContext());
		try {
			List<Address> list = gc.getFromLocation(latitude, longitude, 5);
			int i = 0;
			while (i<list.size()) 
			{
				String temp = list.get(i).getLocality();
				if(temp!=null) {place = temp;}
				i++;
			}
			if(place!=null) {cur_loc.setText(place);}
			else {cur_loc.setText("Unknown");}
		}
		//This is thrown if the phone has no Internet connection.
		catch (IOException e) {
			cur_loc.setText("No location avaliable");
		}
	}

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
		b.putSerializable(Constants.CurrentWorkingEvent, event);
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
				i = new Intent();
				serviceInterface.GiveNewSavedEvent(event);
				String saveData = serviceInterface.Save();
				SharedPreferences sp = getSharedPreferences(Constants.SaveDataName, MODE_PRIVATE);
				Editor spe = sp.edit();
				spe.putString(Constants.SaveDataName, saveData);
				spe.commit();
				b.putBoolean(Constants.IsSavedEventFlag, true);
				i.putExtras(b);
				setResult(RESULT_OK, i);
				finish();
				break;
			}
			case R.id.send_invites_event : {
				serviceInterface.GiveNewSentEvent(event, new IRequestListener<PubEvent>() {
					
					public void onRequestFail(Exception e) {
						// TODO Auto-generated method stub
						
					}
					
					public void onRequestComplete(PubEvent data) {
						Log.d(Constants.MsgInfo, "PubEvent sent, event id: " + data.GetEventId());
						Intent intent = new Intent();
						Bundle b = new Bundle();
						b.putAll(getIntent().getExtras()); 
						b.putSerializable(Constants.CurrentWorkingEvent, data);
						b.putBoolean(Constants.IsSavedEventFlag, false);
						intent.putExtras(b);
						setResult(RESULT_OK, intent);
						finish();						
					}
				});
				
				
				// Inflating the loading bar
				LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
				ViewGroup parent = (ViewGroup) findViewById(R.id.organise_screen);

				View pBar = inflater.inflate(R.layout.loading_bar, parent, false);
				parent.addView(pBar);
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
			event = (PubEvent)data.getExtras().getSerializable(Constants.CurrentWorkingEvent);
			UpdateFromEvent();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuItem edit = menu.add(0, Menu.NONE, 0, "Change Location");
		edit.setOnMenuItemClickListener(this);

		return super.onCreateOptionsMenu(menu);
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
						Geocoder geocoder = new Geocoder(getApplicationContext());
						try {
							List<Address> addresses = geocoder.getFromLocationName(loc.getText().toString(), 5);
							double lat = 0;
							double latsum = 0;
							double lng = 0;
							double lngsum = 0;
							if(addresses!=null) {
								for(int i=0; i<addresses.size(); i++) {
									Address a = addresses.get(i);
									if(a!=null) 
									{
										if(lat==0) {lat = a.getLatitude();}
										else {
											latsum+=a.getLatitude();
											lat=latsum/i;
										}
										if(lng==0) {lng = a.getLongitude();}
										else {
											lngsum+=a.getLongitude();
											lng=lngsum/i;
										}
									}
								}
							}
							if(lat!=0&&lng!=0&&findNewNearestPub(lat,lng)){
								latSet=lat;
								lngSet=lng;
								locSet=true;
								cur_loc.setText(loc.getText()); 
								UpdateFromEvent();
							}
							else {Toast.makeText(getApplicationContext(), "Unrecognised location", Toast.LENGTH_SHORT).show();}
						} 
						catch (IOException e) 
						{
							Log.d(Constants.MsgError,"Error in finding latitude & longitude from given location.");
							e.printStackTrace();
						}
						dialog.cancel();
					}
				})
				.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
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


	private void UpdateFromEvent()
	{
		cur_pub.setText(event.GetPubLocation().pubName);
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
				serviceInterface.addDataRequest(request, new IRequestListener<AppUser>() {

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
	private boolean findNewNearestPub(double lat, double lng) {
		PubFinder finder = new PubFinder(lat,lng);
		try {
			List<Place> list = finder.performSearch();
			PubLocation best = new PubRanker(list).returnBest();
			if(best==null) {return false;}
			event.SetPubLocation(best);
			return true;
		} catch (Exception e) {
			Log.d(Constants.MsgError, "Cannot find pubs based on this location.");
			e.printStackTrace();
			return false;
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
			serviceInterface = (IPubService)service;
			facebook = serviceInterface.GetFacebook();
			UpdateFromEvent();
		}

		public void onServiceDisconnected(ComponentName className)
		{
		}

	};
}

/*class SendData extends AsyncTask<Organise, Integer, Boolean> {
	Organise activity;
	PubEvent event;

	protected Boolean doInBackground(Organise... organise) {

		// Saves the activity to the class
		activity = organise[0];
		event = (PubEvent) activity.getIntent().getExtras().getSerializable(Constants.CurrentWorkingEvent);

		//TODO: Open a socket

		Socket socket = null; 
		try {
			socket = new Socket(Constants.ServerIp, Constants.Port);
		} catch (IOException e) {
			//TODO: Handle exception indicating that connection can't be established
			return false;
		}

		ObjectOutputStream serializer = null;
		ObjectInputStream deserializer = null;

		try {
			serializer = new ObjectOutputStream(socket.getOutputStream());
			deserializer = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			//TODO: Handle Exception When the network is buggered
			return false;
		}

		MessageType t = MessageType.newPubEventMessage;
		try {
			serializer.writeObject(t);
			serializer.writeObject(event);
			serializer.flush();
		} catch (IOException e) {
			//TODO: Handle an exception when we cannae send the data
			return false;
		}

		try {
			AcknoledgementData a = (AcknoledgementData)deserializer.readObject();
			event.SetEventId(a.globalEventId);
		} catch (IOException e) {
			//TODO: Handle when we don't receive a Acknowledgement properly
			return false;
		} catch (ClassNotFoundException e) {
			//TODO: Handle when what we receive aint what we want
		}

		return true;
	}

	protected void onPostExecute(Boolean... b) {
		Intent i = new Intent();


		i.putExtras(activity.getIntent().getExtras());
		activity.setResult(Activity.RESULT_OK, i);
		activity.finish();
	}
}*/

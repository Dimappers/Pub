package dimappers.android.pub;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;

public class Organise extends ListActivity implements OnClickListener{
	
	private Button cur_pub;
	private Button cur_time;
	private TextView cur_loc;
	
	private PubEvent event;
	private User facebookUser;
	
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private ListView guest_list;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState)
	 {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.organise);
	    	
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
	    		
	    		facebookUser = (User)b.getSerializable(Constants.CurrentFacebookUser);
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
	    	
	    	UpdateFromEvent();
	    	
	    	cur_pub.setOnClickListener(this);
	    	
	    	guest_list.setOnItemClickListener(new OnItemClickListener() {
	    	    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
	    	    	Intent i = new Intent(getBaseContext(), Guests.class);
	    	    	Bundle b = new Bundle();
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
	    		else {cur_loc.setText("(" + latitude + "," + longitude + ")");}
	    	}
	    	//This is thrown if the phone has no Internet connection.
	    	catch (IOException e) {
	    		cur_loc.setText("(" + latitude + "," + longitude + ")");
	    	}
	 }
	 
	 public void onClick(View v)
	 {
		Intent i;
		Bundle b = new Bundle();
		b.putAll(getIntent().getExtras()); 
		b.putSerializable(Constants.CurrentWorkingEvent, event);
		 switch (v.getId()){
		 		 case R.id.current_location : {
		 			 //FIXME: need to do this in a way that involves long/lat - if we even want it at all!
				 final EditText loc = new EditText(getApplicationContext());
				 new AlertDialog.Builder(this).setMessage("Enter your current location:")  
		           .setTitle("Change Location")  
		           .setCancelable(true)  
		           .setPositiveButton("Save", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   cur_loc.setText(loc.getText());
		        	   //TODO: turn off location listener
		        	   findNewNearestPub();
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
				 break;
			 }
			case R.id.pub_button : {
				i = new Intent(this, ChoosePub.class);
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
				StoredData storedData = StoredData.getInstance();
				storedData.AddNewSavedEvent(event);
				i.putExtras(b);
				setResult(RESULT_OK, i);
				finish();
				break;
			}
			case R.id.send_invites_event : {
				i = new Intent();
				event.SetEventId(1); //In reality this should be set by server, sent back to the app which fills in actual global id
				StoredData storedData = StoredData.getInstance();
				storedData.AddNewSentEvent(event);
				i.putExtras(b);
				setResult(RESULT_OK, i);
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
			 //We don't actually care what we are returning from, always get the latest event and update the screen
			 event = (PubEvent)data.getExtras().getSerializable(Constants.CurrentWorkingEvent);
			 UpdateFromEvent();
		 }
	 }

	
	private void UpdateFromEvent()
	{
		cur_pub.setText(event.GetPubLocation().pubName);
		cur_time.setText(event.GetStartTime().getTime().toString());
		
		listItems.clear();
    	for(User user : event.GetUsers()) {
    		listItems.add(AppUser.AppUserFromUser(user).GetRealFacebookName());
    	}
    	
    	adapter.notifyDataSetChanged();
	}
	private void findNewNearestPub() {
		//TODO: use cur_loc to find nearest pub (using Google places)
		event.SetPubLocation(new PubLocation()/*new nearest found location*/);
		UpdateFromEvent();
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

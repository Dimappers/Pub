package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;

public class UserInvites extends Activity implements OnClickListener, OnLongClickListener 
{

	PubEvent event;
	AppUser facebookUser;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_invites);
		
		event = (PubEvent)getIntent().getExtras().getSerializable(Constants.CurrentWorkingEvent);
    	if(event == null)
    	{
    		Log.d(Constants.MsgError, "Event missing for showing details about");
    		setResult(Constants.MissingDataInBundle);
    		finish();
    	}
    	facebookUser = (AppUser)getIntent().getExtras().getSerializable(Constants.CurrentFacebookUser);
    	if(facebookUser == null)
    	{
    		Log.d(Constants.MsgError, "Host data missing for showing details about");
    		setResult(Constants.MissingDataInBundle);
    		finish();
    	}
		
    	TextView pubNameText = (TextView) findViewById(R.id.userInvitesPubNameText);
    	pubNameText.setText(event.GetPubLocation().toString());
    	
    	TextView startTime = (TextView) findViewById(R.id.userInviteStartTimeText);
    	startTime.setText(event.GetFormattedStartTime());

    	
    	Button button_going = (Button)findViewById(R.id.going);
    	button_going.setOnClickListener(this);
    	
    	Button button_long_click_going = (Button)findViewById(R.id.going);
    	button_long_click_going.setOnLongClickListener(this);
    	
    	Button button_decline = (Button) findViewById(R.id.decline);
    	button_decline.setOnClickListener(this);
    
    	
    	/*	TODO: Passing values to determine which page loaded this one: going or waiting for response to know the status.
    	  	
    	  	Make available from time textbox open time dialog and automatically have current start time put in
    	     	 	
    	 */

    	ListView list = (ListView) findViewById(R.id.listView2);
    	 
    	
    	/* TODO: Possibly change PubEvent to have hash map to User, ResponseData (at the moment just stores weather
    	 * they said yes or no, need things like msg, time free etc */
    	
    	
    	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
    	
    	for(Entry<User, UserStatus> userResponse : event.GetGoingStatus().entrySet())
    	{
    		HashMap<String, String> map = new HashMap<String, String>();
    
    		String freeFromWhen = event.GetStartTime().get(Calendar.HOUR_OF_DAY) + ":" + event.GetStartTime().get(Calendar.MINUTE);
    		if(userResponse.getValue().freeFrom != null)
    		{
    			freeFromWhen = userResponse.getValue().freeFrom.get(Calendar.HOUR_OF_DAY) + ":" + userResponse.getValue().freeFrom.get(Calendar.MINUTE);
    		}
    		map.put("Available From Time", freeFromWhen);
    		map.put("Guest", AppUser.AppUserFromUser(userResponse.getKey()).GetRealFacebookName());
    		
    		mylist.add(map);
    	}
 
    	//TODO: The SDK says all the things in the last parameter should be text views, ours are not (R.id.envelope) do we need to write our own SimpleAdapter
    	SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.user_row,
    	            new String[] {"Guest", "Available From Time"}, new int[] {R.id.user_guest, R.id.user_time});
    	list.setAdapter(mSchedule);
	}
	
	
	
	public void onClick(View v)
	{	
		switch (v.getId()) {
		case R.id.going : 
		{
	    	
	    	findViewById(R.id.going).setBackgroundColor(Color.GREEN);
	    	findViewById(R.id.decline).setBackgroundResource(android.R.drawable.btn_default);
	    	
			break;
		}
		case R.id.decline :
		{
	    	
	    	findViewById(R.id.decline).setBackgroundColor(Color.RED);
	    	findViewById(R.id.going).setBackgroundResource(android.R.drawable.btn_default);

			break;
		}
		}
    }
	
	public boolean onLongClick(View v)
	{
		switch (v.getId()) {
		case R.id.going :
		{
			showAddDialog();
			return true;
		}
		}
		return false;
	}
	
	private void showAddDialog() 
	{
		 final Dialog commentDialog = new Dialog(UserInvites.this);
         commentDialog.setContentView(R.layout.making_comment);
         commentDialog.setTitle("Do you want to make a comment?");
         commentDialog.setCancelable(true);
		
        TextView text = (TextView) commentDialog.findViewById(R.id.comment_text_box);

		Button attachButton = (Button) commentDialog.findViewById(R.id.attach); 
		Button cancelButton = (Button) commentDialog.findViewById(R.id.cancel); 

		attachButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) { 

		Toast.makeText(getBaseContext(), "Make a comment", Toast.LENGTH_LONG).show(); 
		} 
		}); 

		cancelButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) { 
		commentDialog.dismiss(); 
		} 
		});
		
		commentDialog.show();
	}

}
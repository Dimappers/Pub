package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HostEvents extends Activity implements OnClickListener{

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.host_events);
		
		Button button_send_invites = (Button)findViewById(R.id.send_Invites);
    	button_send_invites.setOnClickListener(this);
    	
    	Button button_make_comment = (Button) findViewById(R.id.make_a_comment);
    	button_make_comment.setOnClickListener(this);
    	
    	Button button_edit = (Button) findViewById(R.id.edit_button);
    	button_edit.setOnClickListener(this);
    	
    	ImageButton button_delete_event = (ImageButton) findViewById(R.id.delete_Event);
    	button_delete_event.setOnClickListener(this);
    	
    	ImageButton button_cancel_event = (ImageButton) findViewById(R.id.cancel_Event);
    	button_cancel_event.setOnClickListener(this);
    	
    	
    	
    	/*Bundle b = getIntent().getExtras();
    	if(b.getSerializable("sent_event")!=null)
    	{
    		sent_event=(PubEvent)b.getSerializable("sent_event");
    		Toast.makeText(getApplicationContext(), "Received event data: " + sent_event.GetHost().getUserId().toString(), Toast.LENGTH_LONG).show();
    		
    	}
    	else{
    		//TODO: when unsent event is sent
	    	int i =  1/0;
    	}*/
    	
    	
    	
    	
    	ListView list = (ListView) findViewById(R.id.listView1);
    	 
    	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("Comment", "yes");
    	map.put("Guest", "Jason Karp");
    	map.put("Available From Time", "8:00 PM");
    	mylist.add(map);
    	map = new HashMap<String, String>();
    	map.put("Comment", "no");
    	map.put("Guest", "Tom Kiley");
    	map.put("Available From Time", "8:15 PM");
    	mylist.add(map);
    	map = new HashMap<String, String>();
    	map.put("Comment", "no");
    	map.put("Guest", "Tom Nicholls");
    	map.put("Available From Time", "9:15 PM");
    	mylist.add(map);
    	map = new HashMap<String, String>();
    	map.put("Comment", "no");
    	map.put("Guest", "Kim Barrett");
    	map.put("Available From Time", "9:00 PM");
    	mylist.add(map);
    	map = new HashMap<String, String>();
    	map.put("Comment", "no");
    	map.put("Guest", "Mark Fearnley");
    	map.put("Available From Time", " ");
    	mylist.add(map);
    
    	SimpleAdapter mSchedule = new SimpleAdapter(this, mylist, R.layout.row,
    	            new String[] {"Comment", "Guest", "Available From Time"}, new int[] {R.id.envelope, R.id.guest, R.id.time});
    	list.setAdapter(mSchedule);
    	
    	/*TODO: Need to have passed two numbers, either 0 or 1 for example telling me whether this page has 
    	 been loaded from host or sendInvites so to know what to hide and show.(delete and cancel image buttons, send and edit button)
		
		TODO: Develop list todo the cool stuff.
		
		TODO: Find good positions for delete/cancel image buttons
		
		TODO: Pop up option menu, which allows you to edit event if you've already sent invites out ONLY.
		*/
	}
	
	public void onClick(View v)
	{
		Intent i;
		
		switch (v.getId()) {
		case R.id.send_Invites : 
		{
			//i = new Intent(this, HostingEvents.class);
			//startActivity(i);
			break;
		}
		case R.id.delete_Event :
		{
			displayAlert();
			break;
		}
		case R.id.cancel_Event :
		{
			displayCancelAlert();
			break;
		}
		case R.id.edit_button :
		{
			PubEvent event = new PubEvent(Calendar.getInstance(), new AppUser(new Integer(1)));
			event.SetPubLocation(new PubLocation());
			
			PubEvent event = new PubEvent(Calendar.getInstance(), new AppUser(new Integer(1)));
			event.SetPubLocation(new PubLocation());
			Bundle bundle = new Bundle();
			bundle.putSerializable("event", event);
			bundle.putInt("test", 1992);
			bundle.putBoolean("NewEvent", false);
			i = new Intent(this, Organise.class);
			i.putExtras(bundle);
			startActivity(i);
			break;
		}
		case R.id.make_a_comment :
		{
			showAddDialog();
			break;
		}
		}
    }
	
	private void showAddDialog() 
	{
		 final Dialog commentDialog = new Dialog(HostEvents.this);
         commentDialog.setContentView(R.layout.making_comment);
         commentDialog.setTitle("Do you want to make a comment?");
         commentDialog.setCancelable(true);
		
		Button attachButton = (Button) commentDialog.findViewById(R.id.attach); 
		Button cancelButton = (Button) commentDialog.findViewById(R.id.cancel); 

		attachButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) { 

		Toast.makeText(getBaseContext(), "Make a comment", 
		Toast.LENGTH_LONG).show(); 
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

	public  void displayAlert()
    {
		new AlertDialog.Builder(this).setMessage("Are you sure you want to delete this event?")  
           .setTitle("Alert")  
           .setCancelable(true)  
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                finish();
                //TODO: Actually deletes the event!!!
           }
           })
           .setNegativeButton("No", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
           })
           .show(); 
           
    }
	
	public  void displayCancelAlert()
    {
		new AlertDialog.Builder(this).setMessage("Are you sure you want to cancel this event? \nThis will send a notification to all guests invited to this event!")  
           .setTitle("Alert")  
           .setCancelable(true)  
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                finish();
                //TODO: Actually deletes the event!!!
           }
           })
           .setNegativeButton("No", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
           })
           .show(); 
           
    }
}

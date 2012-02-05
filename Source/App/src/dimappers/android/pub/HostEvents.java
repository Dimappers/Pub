package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HostEvents extends Activity implements OnClickListener, OnMenuItemClickListener{

	private PubEvent event;
	private GuestListAdapter gadapter;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.host_events);
		
		Button button_send_invites = (Button)findViewById(R.id.send_Invites);
    	button_send_invites.setOnClickListener(this);
    	
    	Button button_edit = (Button) findViewById(R.id.edit_button);
    	button_edit.setOnClickListener(this);
    	
    	ImageButton button_delete_event = (ImageButton) findViewById(R.id.delete_Event);
    	button_delete_event.setOnClickListener(this);
    	
    	ImageButton button_cancel_event = (ImageButton) findViewById(R.id.cancel_Event);
    	button_cancel_event.setOnClickListener(this);
    	
    	
    	
    	Bundle b = getIntent().getExtras();
    	if(b.getSerializable("sent_event")!=null)
    	{
    		event=(PubEvent)b.getSerializable("sent_event");
			findViewById(R.id.send_Invites).setVisibility(View.GONE);
			findViewById(R.id.edit_button).setVisibility(View.GONE);

    		//Toast.makeText(getApplicationContext(), "Received event data: " + event.GetHost().getUserId().toString(), Toast.LENGTH_LONG).show();
    		
    	}
    	else if(b.getSerializable("unsent_event") != null)
    	{
    		event=(PubEvent)b.getSerializable("unsent_event");
			findViewById(R.id.send_Invites).setVisibility(View.VISIBLE);
			findViewById(R.id.edit_button).setVisibility(View.VISIBLE);

    		//Toast.makeText(getApplicationContext(), "Received event data: " + event.GetHost().getUserId().toString(), Toast.LENGTH_LONG).show();
    	}
    	
    	  ListView list = (ListView) findViewById(R.id.listView1);
    	
      	  ArrayList<String> mData = new ArrayList<String>();
      	  
      	
      	  
      	  GuestListAdapter gadapter = new GuestListAdapter(this, mData);
      	  list.setAdapter(gadapter);
          
          
    	//ListView list = (ListView) findViewById(R.id.listView1);
          
    	
    
    	/*SimpleAdapter mSchedule = new SimpleAdapter(this, mData, R.layout.row,
    	            new String[] {"Comment", "Guest", "Available From Time"}, new int[] {R.id.envelope, R.id.guest, R.id.time});
    	list.setAdapter(mSchedule);*/
    	
    	/*TODO: Need to have passed two numbers, either 0 or 1 for example telling me whether this page has 
    	 been loaded from host or sendInvites so to know what to hide and show.(delete and cancel image buttons, send and edit button)
		
		TODO: Develop list todo the cool stuff.
		
		TODO: Find good positions for delete/cancel image buttons
		
		TODO: Pop up option menu, which allows you to edit event if you've already sent invites out ONLY.
		*/
    	
    		
	}
	
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) 
	 {
		 MenuItem edit = menu.add(0, R.id.edit, 0, "Edit");
		 edit.setOnMenuItemClickListener(this);
		 MenuItem delete_event = menu.add(0, R.id.delete_event, 1, "Delete Event");
		 delete_event.setOnMenuItemClickListener(this);
		 MenuItem cancel = menu.add(0, R.id.cancel, 2, "Cancel");
		 cancel.setOnMenuItemClickListener(this);
	    	
		 return super.onCreateOptionsMenu(menu);
		 //return true;
	 }

	 @Override
	 public boolean onMenuItemClick(MenuItem item) {
	  
		Intent i;	 
		 
		switch(item.getItemId()){
	    case(R.id.edit):
	    {
	    	PubEvent event = new PubEvent(Calendar.getInstance(), new AppUser(new Integer(1)));
			event.SetPubLocation(new PubLocation());
			Bundle bundle = new Bundle();
			bundle.putSerializable("event", event);
			bundle.putInt("test", 1992);
			i = new Intent(this, Organise.class);
			i.putExtras(bundle);
			startActivity(i);
			return true;
	    
	    }
	    case(R.id.delete_event):
	    {
	    	displayAlert();
	    	break; 
	    }
	    case(R.id.cancel):
	    {
	    	displayCancelAlert();
	    	break;
	    }
		}
		return false;
	
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
			Bundle bundle = new Bundle();
			bundle.putSerializable("event", event);
			bundle.putInt("test", 1992);
			bundle.putBoolean("NewEvent", false);
			i = new Intent(this, Organise.class);
			i.putExtras(bundle);
			startActivity(i);
			break;
		}
	
		}
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

class GuestListAdapter extends BaseAdapter 
{
	private Context context;
	private ArrayList<String> mData;

	private String[] guests = {"Jason Karp", "Mark Fearnley"};
	private String[] time = {"8:00 PM", "8:15 PM"};

	public GuestListAdapter(Context context, ArrayList<String> mData)
	{
		this.mData = mData;
		this.context = context;
		
	}
	@Override
	public int getCount() {
		return mData.size();
	}
	
	public Object getGuest(int position)
	{

		return guests[position];
	}
	
	public Object getTime(int position)
	{
		
		return time[position];
	}
	

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row, parent, false);
		
		ImageView comment = (ImageView) rowView.findViewById(R.id.envelope);
		TextView guest = (TextView) rowView.findViewById(R.id.guest);
		TextView time = (TextView) rowView.findViewById(R.id.time);
		
		guest.setText(getGuest(position).toString());
		time.setText(getTime(position).toString());
		//if (guest != null) {
			comment.setImageLevel(R.drawable.comment);
		//} else {
		//	comment.setImageLevel(R.drawable.icon);
		//}

		return rowView;
	}

	
}


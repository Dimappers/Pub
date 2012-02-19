package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;


public class HostEvents extends Activity implements OnClickListener, OnMenuItemClickListener{

	private PubEvent event;
	private AppUser facebookUser;
	private GuestListAdapter gadapter;
	private ImageButton comment_made;
	public static boolean sent;

	IPubService serviceInterface;
	

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_events);
		
		bindService(new Intent(this, PubService.class), connection, 0);

		Button button_send_invites = (Button)findViewById(R.id.send_Invites);
		button_send_invites.setOnClickListener(this);

		Button button_edit = (Button) findViewById(R.id.edit_button);
		button_edit.setOnClickListener(this);    	

		ListView list = (ListView) findViewById(R.id.listView1);


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

		sent = !getIntent().getExtras().getBoolean(Constants.IsSavedEventFlag); //if we have saved the event, it has not been sent
		if(sent)
		{
			findViewById(R.id.send_Invites).setVisibility(View.GONE);
			findViewById(R.id.edit_button).setVisibility(View.GONE);
		}
		else
		{
			findViewById(R.id.send_Invites).setVisibility(View.VISIBLE);
			findViewById(R.id.edit_button).setVisibility(View.VISIBLE);
		}
		
		gadapter = new GuestListAdapter(this);
		list.setAdapter(gadapter);
		
		UpdateDataFromEvent();
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
	}

	public boolean onMenuItemClick(MenuItem item) {

		Intent i;	 

		switch(item.getItemId()){
		case(R.id.edit):
		{
			Bundle bundle = new Bundle();
			bundle.putAll(getIntent().getExtras());
			bundle.putSerializable(Constants.CurrentWorkingEvent, event);
			bundle.putBoolean(Constants.IsSavedEventFlag, true);

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
		case R.id.edit_button :
		{
			Bundle bundle = new Bundle();
			bundle.putAll(getIntent().getExtras());
			bundle.putSerializable(Constants.CurrentWorkingEvent, event);
			bundle.putBoolean(Constants.IsSavedEventFlag, true);

			i = new Intent(this, Organise.class);
			i.putExtras(bundle);
			startActivityForResult(i, Constants.FromEdit);
			break;
		}

		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode==RESULT_OK)
    	{
    		event = (PubEvent)data.getExtras().getSerializable(Constants.CurrentWorkingEvent);
    		if(requestCode == Constants.FromEdit)
    		{
    			super.onActivityResult(requestCode, resultCode, data);
    			UpdateDataFromEvent();
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
				serviceInterface.RemoveSavedEvent(event);
				finish();

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

	private void UpdateDataFromEvent()
	{
		TextView pubNameText = (TextView)findViewById(R.id.hostEventsPubName);
		pubNameText.setText(event.GetPubLocation().toString());


		TextView startTimeText = (TextView)findViewById(R.id.hostEventsCurrentStartTime);
		startTimeText.setText(event.GetFormattedStartTime());    	

		gadapter.updateList(event);
		gadapter.notifyDataSetChanged();
	}
	
	private ServiceConnection connection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder)
		{
			serviceInterface = (IPubService)serviceBinder;
			
		}

		public void onServiceDisconnected(ComponentName arg0)
		{			
		}
		
	};
}

class GuestListAdapter extends BaseAdapter 
{
	private Context context;
	private final List<GuestList> mData;	

	public GuestListAdapter(Context context)
	{
		mData = new ArrayList<GuestList>();
		this.context = context;
	}
	public int getCount() {
		return mData.size();
	}

	public Object getItem(int position) {
		return mData.get(position);
	}
	public long getItemId(int position) {
		return position;
	}


	public View getView(int position, View convertView, ViewGroup parent) 
	{
		GuestListView glView = null;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.hosted_row, parent, false);

		glView = new GuestListView();

		ImageView comment = (ImageView) rowView.findViewById(R.id.envelope);
		glView.guest = (TextView) rowView.findViewById(R.id.guest);
		glView.time = (TextView) rowView.findViewById(R.id.time);

		GuestList guestList = mData.get(position);

		glView.guest.setText(guestList.getGuest().toString());
		glView.time.setText(guestList.getTime().toString());

		if(HostEvents.sent == true )
		{
			comment.setImageLevel(R.drawable.email_open);
			comment.setClickable(true);
			comment.setOnClickListener(new OnClickListener() {
				public void onClick(View v) 
				{
					showAddDialog();
				}
			});
		} 
		else
		{
			comment.setVisibility(View.GONE);	
		}

		return rowView;
	}

	//Dialog box for comments received from guests but at moment shows only old comment dialog box.
	private void showAddDialog() 
	{
		final Dialog commentDialog = new Dialog(context);
		commentDialog.setContentView(R.layout.received_comment);
		//commentDialog.setTitle(R.id.title);  //After this should have the user name who sent message 

		ImageButton cancelButton = (ImageButton) commentDialog.findViewById(R.id.cancel_dialog); 

		TextView text = (TextView) commentDialog.findViewById(R.id.comments_received);
		text.setClickable(false);

		cancelButton.setOnClickListener(new OnClickListener() { 
			// @Override 
			public void onClick(View v) { 
				commentDialog.dismiss(); 
			} 
		});

		commentDialog.show();
	}
	
	public void updateList(PubEvent event)
	{
		mData.clear();
		for(Entry<User, UserStatus> userResponse : event.GetGoingStatusMap().entrySet())
		{
			String freeFromWhen = event.GetStartTime().get(Calendar.HOUR_OF_DAY) + ":" + event.GetStartTime().get(Calendar.MINUTE);
			if(userResponse.getValue().freeFrom != null)
			{
				freeFromWhen = userResponse.getValue().freeFrom.get(Calendar.HOUR_OF_DAY) + ":" + userResponse.getValue().freeFrom.get(Calendar.MINUTE);
			}
			mData.add(new GuestList(AppUser.AppUserFromUser(userResponse.getKey()).GetRealFacebookName(), freeFromWhen));  
		}
	}

}

class GuestList
{
	private String guest;
	private String time;

	public GuestList(String guest, String time)
	{
		this.guest = guest;
		this.time = time;
	}

	public void setGuest(String guest)
	{
		this.guest = guest;
	}

	public String getGuest()
	{
		return guest;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public String getTime()
	{
		return time;
	}

}

class GuestListView
{
	protected TextView guest;
	protected TextView time;	
}
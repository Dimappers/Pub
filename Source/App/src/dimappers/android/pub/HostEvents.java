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
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.format.Time;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;
import dimappers.android.pub.UserInvites.GuestAdapter.UpdateList;
import dimappers.android.pub.UserInvites.GuestAdapter.UserUserStatus;


public class HostEvents extends Activity implements OnClickListener, OnMenuItemClickListener{

	private PubEvent event;
	private AppUser facebookUser;
	private GuestListAdapter gadapter;
	private ImageButton comment_made;
	public static boolean sent;

	IPubService service;
	

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_events);
		
		bindService(new Intent(this, PubService.class), connection, 0);

		Button button_send_invites = (Button)findViewById(R.id.send_Invites);
		button_send_invites.setOnClickListener(this);

		Button button_edit = (Button) findViewById(R.id.edit_button);
		button_edit.setOnClickListener(this);  
		
		Button button_itson = (Button) findViewById(R.id.it_is_on);
		button_itson.setOnClickListener(this);

		ListView list = (ListView) findViewById(R.id.listView1);

		sent = !getIntent().getExtras().getBoolean(Constants.IsSavedEventFlag); //if we have saved the event, it has not been sent
		if(sent)
		{
			button_send_invites.setVisibility(View.GONE);
			button_edit.setVisibility(View.GONE);
			button_itson.setVisibility(View.VISIBLE);
		}
		else
		{
			button_send_invites.setVisibility(View.VISIBLE);
			button_edit.setVisibility(View.VISIBLE);
			button_itson.setVisibility(View.GONE);
		}
		
		gadapter = new GuestListAdapter(this);
		list.setAdapter(gadapter);
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
			bundle.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
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
			service.GiveNewSentEvent(event, new IRequestListener<PubEvent>() {
				
				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, "Could not send event");
				}
				
				public void onRequestComplete(PubEvent data) {
					Log.d(Constants.MsgInfo, "PubEvent sent, event id: " + data.GetEventId());
					HostEvents.this.runOnUiThread(new Runnable()
					{
						public void run() {
							findViewById(R.id.send_Invites).setVisibility(View.GONE);
							findViewById(R.id.edit_button).setVisibility(View.GONE);
							findViewById(R.id.it_is_on).setVisibility(View.VISIBLE);
							UpdateDataFromEvent();
						}
						
					});
				}
			});
			break;
		}
		case R.id.edit_button :
		{
			Bundle bundle = new Bundle();
			bundle.putAll(getIntent().getExtras());
			bundle.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			bundle.putBoolean(Constants.IsSavedEventFlag, true);

			i = new Intent(this, Organise.class);
			i.putExtras(bundle);
			startActivityForResult(i, Constants.FromEdit);
			break;
		}
		case R.id.it_is_on :
		{
			event.setCurrentStatus(EventStatus.itsOn);
			DataRequestConfirmDeny request = new DataRequestConfirmDeny(event);
			service.addDataRequest(request, new IRequestListener<PubEvent>() {

				@Override
				public void onRequestComplete(PubEvent data) {
					if(data != null)
					{
						event = data;
					}
					
				}

				@Override
				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, e.getMessage());					
				}
			});
			
			TextView tv = (TextView)findViewById(R.id.hostEventTimeTillPubText);
			v.setVisibility(View.GONE);
			tv.setVisibility(View.VISIBLE);
			
			TimeTillPub timer = new TimeTillPub(event.GetStartTime(), tv);
			timer.start();
		}

		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode==RESULT_OK)
    	{
    		if(requestCode == Constants.FromEdit)
    		{
    			event = service.getEvent(data.getExtras().getInt(Constants.CurrentWorkingEvent));
    			super.onActivityResult(requestCode, resultCode, data);
    			UpdateDataFromEvent();
    		}
    	}
    }

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}
	
	public  void displayAlert()
	{
		new AlertDialog.Builder(this).setMessage("Are you sure you want to delete this event?")  
		.setTitle("Alert")  
		.setCancelable(true)  
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				service.RemoveSavedEvent(event);
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
		pubNameText.setText(event.GetPubLocation().getName());


		TextView startTimeText = (TextView)findViewById(R.id.hostEventsCurrentStartTime);
		startTimeText.setText(event.GetFormattedStartTime());    	

		gadapter.updateList(event);
		gadapter.notifyDataSetChanged();
		
		if(event.getCurrentStatus() == EventStatus.itsOn)
		{
			TextView tv = (TextView)findViewById(R.id.hostEventTimeTillPubText);
			View v = findViewById(R.id.it_is_on);
			v.setVisibility(View.GONE);
			tv.setVisibility(View.VISIBLE);
			
			TimeTillPub timer = new TimeTillPub(event.GetStartTime(), tv);
			timer.start();
		}
	}
	
	private ServiceConnection connection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder)
		{
			service = (IPubService)serviceBinder;
			event = service.getEvent(getIntent().getExtras().getInt(Constants.CurrentWorkingEvent));
			facebookUser = service.GetActiveUser();
			
			UpdateDataFromEvent();
		}

		public void onServiceDisconnected(ComponentName arg0)
		{			
		}
		
	};
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
			for(final Entry<User, UserStatus> userResponse : event.GetGoingStatusMap().entrySet())
			{
				final String freeFromWhen;
				if(userResponse.getValue().freeFrom != null)
				{
					freeFromWhen = PubEvent.GetFormattedDate(userResponse.getValue().freeFrom);
				}
				else
				{
					freeFromWhen = event.GetFormattedStartTime();
				}
				if(userResponse.getKey() instanceof AppUser)
				{
					mData.add(new GuestList(((AppUser)userResponse.getKey()).toString(), freeFromWhen));  
				}
				else
				{
					//TODO: Untested - should work but would require an event where we don't already have the users downloaded - ie host an event, loose it locally then reget from server
					DataRequestGetFacebookUser getUser = new DataRequestGetFacebookUser(userResponse.getKey().getUserId());
	    			service.addDataRequest(getUser, new IRequestListener<AppUser>() {

						public void onRequestComplete(AppUser data) {
							
							HostEvents.this.runOnUiThread(new UpdateList(data, freeFromWhen));
						}

						public void onRequestFail(Exception e) {
							// TODO Auto-generated method stub
							
						}
						
						class UpdateList implements Runnable
						{
							GuestList glEntry;
							public UpdateList(AppUser data, String freeFromWhen)
							{
								glEntry = new GuestList(data.toString(), freeFromWhen);
							}
							
							public void run() {
								// TODO Auto-generated method stub
								mData.add(glEntry);
								notifyDataSetChanged();
							}
							
						}
	    				
	    			});
				}
				
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
	
	class TimeTillPub extends CountDownTimer	
	{
		private TextView textView;
		public TimeTillPub(Calendar pubStartTime, TextView countdownTextView)
		{
			super(pubStartTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), 10);
			textView = countdownTextView;
		}
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			textView.setText("It's on!");
		}

		@Override
		public void onTick(long millisUntilFinished) {
			int hours, minutes, seconds, miliseconds;
			
			hours = (int)(millisUntilFinished / (60 * 60 * 1000));
			millisUntilFinished -= hours * 60 * 60 * 1000;
			minutes = (int)(millisUntilFinished / (60 * 1000));
			millisUntilFinished -= minutes * 60 * 1000;
			seconds = (int)(millisUntilFinished / 1000);
			millisUntilFinished -= seconds * 1000;
			miliseconds = (int)millisUntilFinished;
			
			int microSeconds = (int)(miliseconds / 10);
			String formattedMicroseconds;
			if(microSeconds < 10)
			{
				formattedMicroseconds = "0" + Integer.toString(microSeconds);
			}
			else
			{
				formattedMicroseconds = Integer.toString(microSeconds);
			}
			
			textView.setText(hours + ":" + minutes + ":" + seconds + ":" + formattedMicroseconds + " until beer");			
		}
	}
}
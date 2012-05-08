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
import android.view.Window;
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
import dimappers.android.PubData.GoingStatus;
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
		if(event!=null)
		{
			//FIXME: should refresh when sent is hit.
			if(event.GetEventId()<0)
			{
				MenuItem delete_event = menu.add(0, R.id.delete_event, 1, "Delete Event");
				delete_event.setOnMenuItemClickListener(this);
			}
			else
			{
				MenuItem cancel = menu.add(0, R.id.cancel, 1, "Cancel");
				cancel.setOnMenuItemClickListener(this);
				
				MenuItem refresh = menu.add(0,R.id.refresh_event, 2, "Refresh");
				refresh.setOnMenuItemClickListener(this);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.removeItem(R.id.cancel);
		menu.removeItem(R.id.delete_event);
		menu.removeItem(R.id.refresh_event);
		if(sent||(event!=null&&event.GetEventId()>=0))
		{ 
			MenuItem cancel = menu.add(0, R.id.cancel, 1, "Cancel");
			cancel.setOnMenuItemClickListener(this);
			
			MenuItem refresh = menu.add(0,R.id.refresh_event, 2, "Refresh");
			refresh.setOnMenuItemClickListener(this);
		}
		else
		{
			MenuItem delete_event = menu.add(1, R.id.delete_event, 1, "Delete Event");
			delete_event.setOnMenuItemClickListener(this);
		}
		return super.onPrepareOptionsMenu(menu);
		
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
			startActivityForResult(i,0);
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
		
		case(R.id.refresh_event):
		{
			DataRequestGetLatestAboutPubEvent refresher = new DataRequestGetLatestAboutPubEvent(event.GetEventId());
			service.addDataRequest(refresher, new IRequestListener<PubEvent>(){

				public void onRequestComplete(PubEvent data) {
					
					event = data;
					runOnUiThread(new Runnable(){

						public void run() {
							UpdateDataFromEvent();			
						}
				
					});					
					
				}

				public void onRequestFail(Exception e) {
					// TODO Auto-generated method stub
					e.printStackTrace();
				}
				
			});
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
					Log.d(Constants.MsgError, "Could not sent invite: " + e.getMessage());
					runOnUiThread(new Runnable(){
						public void run() {
							Toast.makeText(getApplicationContext(),"Unable to send event, please try again later.",Toast.LENGTH_LONG).show();
							//FIXME: probably should make it more obvious when this fails
						}});
				}
				
				public void onRequestComplete(PubEvent data) {
					Log.d(Constants.MsgInfo, "PubEvent sent, event id: " + data.GetEventId());
					sent = true;
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

				public void onRequestComplete(PubEvent data) {
					if(data != null)
					{
						event = data;
						runOnUiThread(new Runnable(){
							public void run() {
								UpdateDataFromEvent();			
							}
						});	
					}
					
				}

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
    		}
    	}
    	UpdateDataFromEvent();
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
				service.RemoveEventFromStoredDataAndCancelNotification(event);
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
				service.CancelEvent(event);
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
			int eventId = getIntent().getExtras().getInt(Constants.CurrentWorkingEvent);
			event = service.getEvent(eventId);
			facebookUser = service.GetActiveUser();
			
			UpdateDataFromEvent();
			
			DataRequestGetLatestAboutPubEvent refresher = new DataRequestGetLatestAboutPubEvent(event.GetEventId());
			service.addDataRequest(refresher, new IRequestListener<PubEvent>(){

				public void onRequestComplete(PubEvent data) {
					
					event = data;
					runOnUiThread(new Runnable(){

						public void run() {
							UpdateDataFromEvent();			
						}
				
					});					
					
				}

				public void onRequestFail(Exception e) {
					// TODO Auto-generated method stub
					e.printStackTrace();
				}
				
			});
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


		public View getView(final int position, View convertView, ViewGroup parent) 
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.hosted_row, parent, false);
			
			ImageView comment = (ImageView) convertView.findViewById(R.id.envelope);
			TextView guest = (TextView)convertView.findViewById(R.id.guest);
			TextView time = (TextView)convertView.findViewById(R.id.time);

			GuestList guestList = mData.get(position);

			comment.setVisibility(View.INVISIBLE);		
			guest.setText(guestList.getGuest().toString());
			time.setText(guestList.getTime().toString());
			
			
			if(HostEvents.sent == true )
			{
				for(final Entry<User, UserStatus> userResponse : event.GetGoingStatusMap().entrySet())  //since each row is returned, maybe should be looking at guest in row
				{
					if(userResponse.getValue().messageToHost != "" && userResponse.getValue().messageToHost != null)
					{						

						if(guestList.getGuestId() == userResponse.getKey().getUserId())
						{
							comment.setVisibility(View.VISIBLE);
							comment.setImageLevel(R.drawable.email_open);
							comment.setClickable(true);
							comment.setOnClickListener(new OnClickListener() {
								public void onClick(View v) 
								{
									showAddDialog(position, v);
								}
							});	
							
							convertView.setClickable(true);
							convertView.setOnClickListener(new OnClickListener() {
								public void onClick(View v)
								{
									showAddDialog(position,v);
								}
							});
							break;
						}
						else
							comment.setVisibility(View.INVISIBLE);
					}
					else 
						comment.setVisibility(View.INVISIBLE);
							
				}
			} 
			else
			{
				comment.setVisibility(View.GONE);	
			}

			return convertView;
		}

		//Dialog box for comments received from guests but at moment shows only old comment dialog box.
		private void showAddDialog(int position, View view) 
		{
			final Dialog commentDialog = new Dialog(context);
			commentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			commentDialog.setContentView(R.layout.received_comment);
			TextView title = (TextView) commentDialog.findViewById(R.id.name);  //After this should have the user name who sent message 
			
			TextView text = (TextView) commentDialog.findViewById(R.id.messageText);
			GuestList guestList = mData.get(position);
			
						
			for(final Entry<User, UserStatus> userResponse : event.GetGoingStatusMap().entrySet())
			{
				if(guestList.getGuestId() == userResponse.getKey().getUserId())
				{
					title.setText(guestList.getGuest().toString());
					text.setText(guestList.getMessage());
				}
					
			}

			commentDialog.show();
		}
		
		public void updateList(PubEvent event)
		{
			mData.clear();

			for(final Entry<User, UserStatus> userResponse : event.GetGoingStatusMap().entrySet())
			{
				final String freeFromWhen;
				final String message;
				
				if(userResponse.getValue().messageToHost != null && userResponse.getValue().messageToHost != "")
				{
					message = userResponse.getValue().messageToHost;
				}			
				else 
					message = "";
				
				
				if(userResponse.getValue().goingStatus == GoingStatus.going)
				{
					if(userResponse.getValue().freeFrom != null 
							&& userResponse.getValue().freeFrom.getTimeInMillis() != event.GetStartTime().getTimeInMillis()
							&& userResponse.getValue().freeFrom.after(event.GetStartTime()))
					{
						freeFromWhen = PubEvent.GetFormattedDate(userResponse.getValue().freeFrom);
					}
					else
					{
						freeFromWhen = "Up for it!";
					}
				}
				else if(userResponse.getValue().goingStatus == GoingStatus.notGoing)
				{
					freeFromWhen ="Nah";
				}
				else
				{
					freeFromWhen = "";
				}
				if(userResponse.getKey() instanceof AppUser)
				{
					mData.add(new GuestList(((AppUser)userResponse.getKey()), freeFromWhen, message));  
				}
				else
				{
					//TODO: Untested - should work but would require an event where we don't already have the users downloaded - ie host an event, loose it locally then reget from server
					DataRequestGetFacebookUser getUser = new DataRequestGetFacebookUser(userResponse.getKey().getUserId());
	    			service.addDataRequest(getUser, new IRequestListener<AppUser>() {

						public void onRequestComplete(AppUser data) {
							
							HostEvents.this.runOnUiThread(new UpdateList(data, freeFromWhen, message));
						}

						public void onRequestFail(Exception e) {
							// TODO Auto-generated method stub
							
						}
						
						class UpdateList implements Runnable
						{
							GuestList glEntry;
							public UpdateList(AppUser data, String freeFromWhen, String message)
							{
								glEntry = new GuestList(data, freeFromWhen, message);
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
		private AppUser guest;
		private String time;
		private String message;
		
		public GuestList(AppUser guest, String time, String message)
		{
			this.guest = guest;
			this.time = time;
			this.message = message;
		}

		public void setGuest(AppUser guest)
		{
			this.guest = guest;
		}

		public AppUser getGuest()
		{
			return guest;
		}
		
		public long getGuestId()
		{
			return guest.getUserId();
		}

		public void setTime(String time)
		{
			this.time = time;
		}

		public String getTime()
		{
			return time;
		}
		
		public void setMessage(String message)
		{
			this.message = message;
		}
		
		public String getMessage()
		{
			return message;
		}

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
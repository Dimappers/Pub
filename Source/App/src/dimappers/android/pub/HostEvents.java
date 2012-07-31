package dimappers.android.pub;

import java.util.Calendar;
import java.util.List;
import net.awl.appgarden.sdk.AppGardenAgent;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UserStatus;


public class HostEvents extends EventScreen {
	
	public static boolean sent;
	 
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_events);
		
		AppGardenAgent.passExam("LOADED HOST EVENTS");

		Button button_send_invites = (Button)findViewById(R.id.send_Invites);
		button_send_invites.setOnClickListener(this);

		Button button_edit = (Button) findViewById(R.id.edit_button);
		button_edit.setOnClickListener(this);  
		
		Button button_itson = (Button) findViewById(R.id.it_is_on);
		button_itson.setOnClickListener(this);
		
		Typeface font = Typeface.createFromAsset(getAssets(), "SkratchedUpOne.ttf");
		((TextView)findViewById(R.id.PubName)).setTypeface(font);
    	((TextView)findViewById(R.id.StartTime)).setTypeface(font);
    	((TextView)findViewById(R.id.guestHeader)).setTypeface(font);
    	((Button)findViewById(R.id.edit_button)).setTypeface(font);
    	((Button)findViewById(R.id.send_Invites)).setTypeface(font);
    	((Button)findViewById(R.id.it_is_on)).setTypeface(font);
    	((TextView)findViewById(R.id.hostEventTimeTillPubText)).setTypeface(font);
    	
		ListView list = (ListView) findViewById(android.R.id.list);

		registerForContextMenu(list);
				
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
	}

	 
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
	
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(event.GetEventId()>=0)
		{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.invited_hold_menu, menu);
		
		int pos = ((AdapterContextMenuInfo)menuInfo).position;
		
		menu.setHeaderTitle("Respond for " + ((AppUser)gAdapter.getItem(pos)));
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		
		final PubEvent backupEvent = new PubEvent(event.writeXml());
		
		item.getMenuInfo();
	    int itemPosition = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
	    
	    AppUser person = (AppUser) gAdapter.getItem(itemPosition);
	    
	    boolean isGoing = (item.getItemId() == R.id.invited_menu_item_up);
	    
	    if(isGoing)
	    {
	    	event.GetGoingStatusMap().put(person, new UserStatus(GoingStatus.going, event.GetStartTime(), ""));
	    }
	    else
	    {
	    	event.GetGoingStatusMap().put(person, new UserStatus(GoingStatus.notGoing, event.GetStartTime(), ""));
	    }

		service.addDataRequest(new DataRequestSendResponse(new ResponseData(person, event.GetEventId(), isGoing)), new IRequestListener<PubEvent>() {
			
			/*
			 * FIXME: 
			 * 		KB: on mine this always throws a Connection Reset By Peer by that may be the dodgy internet/server falling over - more testing is required.
			 * 			It does update the server though & refreshing the event on the phone shows the update. No idea what is going on there... :S
			 */
			
			public void onRequestComplete(PubEvent data) {
				event = data;
				updateScreen();
			}

			public void onRequestFail(Exception e) {
				Log.d(Constants.MsgError, e.getMessage());
				
				event = backupEvent;
				
				runOnUiThread(new Runnable() {
					
					public void run() {
						Toast.makeText(HostEvents.this, "Error sending response.", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	    
		return true;		
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
			displayDeleteAlert();
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
							updateScreen();			
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
			service.SendEvent(event, new IRequestListener<PubEvent>() {
				
				
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
							updateScreen();
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
								updateScreen();			
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
    	updateScreen();
    }
	
	private void displayDeleteAlert()
	{
		new AlertDialog.Builder(this).setMessage("Are you sure you want to delete this event?")  
		.setTitle("Alert")  
		.setCancelable(true)  
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			 
			public void onClick(DialogInterface dialog, int id) {
				service.DeleteEvent(event);
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

	private void displayCancelAlert()
	{
		new AlertDialog.Builder(this).setMessage("Are you sure you want to cancel this event? \nThis will send a notification to all guests invited to this event!")  
		.setTitle("Alert")  
		.setCancelable(true)  
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			 
			public void onClick(DialogInterface dialog, int id) {
				service.CancelEvent(event, new IRequestListener<PubEvent>() {
					
					public void onRequestFail(Exception e)
					{
						// TODO Auto-generated method stub
						
					}
					
					public void onRequestComplete(PubEvent data)
					{
						// TODO Auto-generated method stub
						
					}
				});
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

	protected void updateScreen()
	{
		super.updateScreen();
		
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
	
	public HostEvents()
	{
		super();
		connection = new HostEventsEventServiceConnection();
	}
	
	private class HostEventsEventServiceConnection extends EventServiceConnection
	{
		 
		public void onServiceConnected(ComponentName name, IBinder serviceBinder)
		{
			super.onServiceConnected(name, serviceBinder);
			
			gAdapter = new HostEventsGuestListAdapter(createAppUserList());
			setListAdapter(gAdapter);
			
			updateScreen();
		}
		
	};
	class HostEventsGuestListAdapter extends GeneralGuestListAdapter 
	{
		//private final List<AppUser> mData;	
		
		public HostEventsGuestListAdapter(List<AppUser> objects) 
		{
			super(HostEvents.this, R.layout.hosted_row, R.id.guest, objects);
			//mData = objects;
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) 
		{
			convertView = super.getView(position, convertView, parent);
			
			ImageView comment = (ImageView) convertView.findViewById(R.id.envelope);

			AppUser appUser = getItem(position);
			UserStatus uStatus = event.GetGoingStatusMap().get(appUser);

			if(uStatus.goingStatus == GoingStatus.going)
			{	
				if(uStatus.messageToHost != null && uStatus.messageToHost == null)
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
				}
			}
			else
			{
				comment.setVisibility(View.INVISIBLE);
			}
			
			return convertView;
		}

		//Dialog box for comments received from guests but at moment shows only old comment dialog box.
		private void showAddDialog(int position, View view) 
		{
			final Dialog commentDialog = new Dialog(HostEvents.this);
			commentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			commentDialog.setContentView(R.layout.received_comment);
			TextView title = (TextView) commentDialog.findViewById(R.id.name);  //After this should have the user name who sent message 
			
			TextView text = (TextView) commentDialog.findViewById(R.id.messageText);
			
			AppUser appUser = getItem(position);
			
			UserStatus uStatus = event.GetGoingStatusMap().get(appUser);
			
			title.setText(appUser.toString());
			text.setText(uStatus.messageToHost);

			commentDialog.show();
		}
		
		/*public void updateList(PubEvent event)
		{	
			mData.clear();
			notifyDataSetChanged();

			Set<User> users = event.GetUsers();
			for(final User user : users)
			{
				service.GetAppUserFromUser(user, new IRequestListener<AppUser>() {

					public void onRequestComplete(AppUser data) {
						if(!mData.contains(data))
						{
							mData.add(data);
						}
						runOnUiThread(new Runnable() {
							
							public void run() {
								notifyDataSetChanged();
							}
						});
					}

					public void onRequestFail(Exception e) {

						e.printStackTrace();
						Log.d(Constants.MsgError, "ERROR creating AppUser for User " + user.getUserId());
					}
				});
			}
			
			notifyDataSetChanged();
		}*/

	}

	class TimeTillPub extends CountDownTimer	
	{
		private TextView textView;
		public TimeTillPub(Calendar pubStartTime, TextView countdownTextView)
		{
			super(pubStartTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), 10);
			textView = countdownTextView;
		}
		 
		public void onFinish() {
			// TODO Auto-generated method stub
			textView.setText("It's on!");
		}

		 
		public void onTick(long millisUntilFinished) {
			int hours, minutes, seconds, miliseconds;
			
			hours = (int)(millisUntilFinished / (60 * 60 * 1000));
			millisUntilFinished -= hours * 60 * 60 * 1000;
			minutes = (int)(millisUntilFinished / (60 * 1000));
			millisUntilFinished -= minutes * 60 * 1000;
			seconds = (int)(millisUntilFinished / 1000);
			millisUntilFinished -= seconds * 1000;
			miliseconds = (int)millisUntilFinished;
			
			int microSeconds = (miliseconds / 10);
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
package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;

public class UserInvites extends Activity implements OnClickListener, OnLongClickListener, OnMenuItemClickListener 
{

	PubEvent event;
	AppUser facebookUser;
	
	IPubService service;
	
	GuestAdapter gAdapter;
	
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		bindService(new Intent(this, PubService.class), connection, 0);
		
		setContentView(R.layout.user_invites);
		Typeface font = Typeface.createFromAsset(getAssets(), "SkratchedUpOne.ttf");
		
    	Button button_going = (Button)findViewById(R.id.going);
    	button_going.setOnClickListener(this);
    	
    	Button button_long_click_going = (Button)findViewById(R.id.going);
    	button_long_click_going.setOnLongClickListener(this);
    	
    	Button button_decline = (Button) findViewById(R.id.decline);
    	button_decline.setOnClickListener(this);
    	
    	((TextView)findViewById(R.id.userInvitesPubNameText)).setTypeface(font);
    	((TextView)findViewById(R.id.userInviteStartTimeText)).setTypeface(font);
    	((TextView)findViewById(R.id.userInviteHostNameText)).setTypeface(font);
    	((TextView)findViewById(R.id.guestHeader)).setTypeface(font);
    	((Button)findViewById(R.id.decline)).setTypeface(font);
    	((Button)findViewById(R.id.going)).setTypeface(font);
    	
    	
    	String firstString = "Up for it!";
    	String secondString = "\nhold for options";
    	
    	SpannableStringBuilder stringBuilder = new SpannableStringBuilder(firstString + secondString);
    	stringBuilder.setSpan(new TextAppearanceSpan(button_going.getContext(), android.R.style.TextAppearance_Medium), 0, firstString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    	stringBuilder.setSpan(new TextAppearanceSpan(button_going.getContext(), android.R.style.TextAppearance_Small), 0, secondString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    	button_going.setText(stringBuilder);
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		if(event!=null)
		{
			//FIXME: should refresh when sent is hit.			
			MenuItem refresh = menu.add(0,R.id.refresh_event, 2, "Refresh");
			refresh.setOnMenuItemClickListener(this);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	
	
	public boolean onMenuItemClick(MenuItem arg0) {
		switch(arg0.getItemId())
		{
			case R.id.refresh_event:
			{
				service.addDataRequest(new DataRequestGetLatestAboutPubEvent(event.GetEventId()), new IRequestListener<PubEvent>(){

					
					public void onRequestFail(Exception e) {
						Log.d(Constants.MsgError, "Error when refreshing event: " + e.getMessage());
					}

					
					public void onRequestComplete(PubEvent data) {
						event = data;
						runOnUiThread(new Runnable(){

							
							public void run() {
								updateScreen();
							}});
					}});
				return true;
			}
		}
		
		return false;
	}
	
	
	public void onClick(View v)
	{	
		switch (v.getId()) {
		case R.id.going : 
		{
	    	
	    	//findViewById(R.id.going).setBackgroundColor(Color.GREEN);
	    	//findViewById(R.id.decline).setBackgroundResource(android.R.drawable.btn_default);
	    	sendResponse(true,event.GetStartTime(),"");
	    	break;
		}
		case R.id.decline :
		{
	    	
	    	//findViewById(R.id.decline).setBackgroundColor(Color.RED);
	    	//findViewById(R.id.going).setBackgroundResource(android.R.drawable.btn_default);
	    	sendResponse(false,event.GetStartTime(),"");
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
	
	TextView timeText;
	
	private void showAddDialog() 
	{
		 final Dialog commentDialog = new Dialog(UserInvites.this);
         commentDialog.setContentView(R.layout.making_comment);
         commentDialog.setTitle("Do you want to make a comment?");
         commentDialog.setCancelable(true);
		

		Button attachButton = (Button) commentDialog.findViewById(R.id.attach); 
		Button cancelButton = (Button) commentDialog.findViewById(R.id.cancel);
		
		timeText = (TextView) commentDialog.findViewById(R.id.changeTime);
		Calendar startTime = event.GetStartTime();
		String ampm="AM";
		if (startTime.get(Calendar.AM_PM)==1) {ampm = "PM";}
		String min = Integer.toString(startTime.get(Calendar.MINUTE));
		if(startTime.get(Calendar.MINUTE)==0) {min = "00";}
		timeText.setText(startTime.get(Calendar.HOUR) + ":" + min + " " + ampm);
	    timeText.setOnClickListener(new OnClickListener() {
	    	
			public void onClick(View v) {
	    		Intent i = new Intent(UserInvites.this, ChooseTime.class);
	    		Bundle b = new Bundle();
	    		b.putBoolean(Constants.HostOrNot, false);
	    		b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
	    		i.putExtras(b);
	    		startActivityForResult(i, Constants.MessageTimeSetter);
	    	}
	    });

	    attachButton.setOnClickListener(new OnClickListener() {  
	    	
			public void onClick(View v) 
	    	{ 
	    		TextView text = (TextView) commentDialog.findViewById(R.id.comment_text_box);

	    		String commentMade = text.getText().toString();

	    		if(timeSet==0||timeSet==event.GetStartTime().getTimeInMillis())
	    		{
	    			sendResponse(true,event.GetStartTime(),commentMade);
	    		}
	    		else 
	    		{
	    			Calendar time = Calendar.getInstance();
	    			time.setTime(new Date(timeSet));
	    			sendResponse(true, time, commentMade);
	    		}

	    		commentDialog.dismiss();
	    	} 
	    }); 

	    cancelButton.setOnClickListener(new OnClickListener() {  
	    	
			public void onClick(View v) 
	    	{ 
	    		commentDialog.dismiss(); 
	    	} 
	    });

	    commentDialog.show();
	}
	
	long timeSet = 0;
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK)
		{
			timeSet = data.getExtras().getLong(Constants.ChosenTime);
			Date date = new Date(timeSet);
			int hour = date.getHours();
			String ampm;
			if(hour>12) {hour -= 12; ampm = "PM";}
			else {ampm = "AM";}
			String minutes = Integer.toString(date.getMinutes());
			if(date.getMinutes()==0) {minutes = "00";}
			timeText.setText(hour + ":" + minutes + " " + ampm);
		}
	}
	
	private ServiceConnection connection = new ServiceConnection()
	{
		
		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder)
		{
			service = (IPubService)serviceBinder;
			
			event = service.getEvent(getIntent().getExtras().getInt(Constants.CurrentWorkingEvent));
			
			if(event == null)
			{
				Toast.makeText(getApplicationContext(), "Could not find event", 2000).show();
				finish();
				return;
			}
			
			service.addDataRequest(new DataRequestGetLatestAboutPubEvent(event.GetEventId()), new IRequestListener<PubEvent>(){

				
				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, "Error when refreshing event: " + e.getMessage());
				}

				
				public void onRequestComplete(PubEvent data) {
					event = data;
					runOnUiThread(new Runnable(){

						
						public void run() {
							updateScreen();
						}});
				}});
					
			facebookUser = service.GetActiveUser();
			
			ListView list = (ListView) findViewById(R.id.listView2);
			gAdapter = new GuestAdapter(event, service); 
			list.setAdapter(gAdapter);
			
			updateScreen();
		}

		
		public void onServiceDisconnected(ComponentName arg0){}
		
	};
	

	private void updateScreen()
	{
		/*switch(event.GetUserGoingStatus(service.GetActiveUser()))
		{
			case going : {findViewById(R.id.going).setBackgroundColor(Color.GREEN); break;}
			case notGoing : {findViewById(R.id.decline).setBackgroundColor(Color.RED); break;}
		}*/
		
		TextView pubNameText = (TextView) findViewById(R.id.userInvitesPubNameText);
    	//pubNameText.setText(event.GetPubLocation().toString());
    	pubNameText.setText(event.GetPubLocation().getName());
    	
    	TextView startTime = (TextView) findViewById(R.id.userInviteStartTimeText);
    	startTime.setText(event.GetFormattedStartTime());
    	
    	try {
			((TextView)findViewById(R.id.userInviteHostNameText)).setText(
					getString(R.string.host_name)
					+ " " +
					AppUser.AppUserFromUser(event.GetHost(), service.GetFacebook()).toString());
		} catch (Exception e) {
			((TextView)findViewById(R.id.userInviteHostNameText)).setText(getString(R.string.host_name)+" unknown");
			e.printStackTrace();
		}
    	
		ListView list = (ListView) findViewById(R.id.listView2);
		gAdapter = new GuestAdapter(event, service); 
		list.setAdapter(gAdapter);
	}
	
	private void sendResponse(boolean going, Calendar freeFromWhen, final String msgToHost)
	{
		DataRequestSendResponse response = new DataRequestSendResponse(going, event.GetEventId(), freeFromWhen, msgToHost);
		
		//Work around: we should get updated event back from the server and refresh from that 
		event.UpdateUserStatus(new ResponseData(service.GetActiveUser(), event.GetEventId(), going, freeFromWhen, msgToHost));
		updateScreen();
		service.addDataRequest(response, new IRequestListener<PubEvent>() {
					
					public void onRequestComplete(PubEvent data) {
						if(data != null)
						{
							event = data;
							runOnUiThread(new Runnable()
							{
								
								public void run() {
									updateScreen();
						    		if(msgToHost!=null&&!msgToHost.equals(""))
						    		{
						    			try {
											Toast.makeText(
													getBaseContext(), 
													"Sent message \"" + msgToHost + "\" to " + AppUser.AppUserFromUser(event.GetHost(), service.GetFacebook()).toString() + ".", 
													Toast.LENGTH_LONG
													).show();
										} catch (Exception e) {
											Toast.makeText(
													getBaseContext(), 
													"Sent message \"" + msgToHost + "\" to host.", 
													Toast.LENGTH_LONG
													).show();
										}
						    		} 
								}
								
							});
						}
					}

					
					public void onRequestFail(Exception e) {
						Log.d(Constants.MsgError, e.getMessage());	
						runOnUiThread(new Runnable(){

							
							public void run() {
								Toast.makeText(getApplicationContext(), "Sending failed.", Toast.LENGTH_LONG).show();
								updateScreen();
							}});
					}
				});
	}
	
	class GuestAdapter extends BaseAdapter
	{
		final ArrayList<UserUserStatus> mylist;
		final PubEvent event;
		
		public GuestAdapter(final PubEvent event, IPubService service)
		{
			this.event = event;
			mylist = new ArrayList<UserUserStatus>();
			
			Set<Entry<User, UserStatus>> asd = event.GetGoingStatusMap().entrySet();
			for(final Entry<User, UserStatus> userResponse : asd)
	    	{	    
	    		if(userResponse.getKey() instanceof AppUser)
	    		{
	    			mylist.add(new UserUserStatus((AppUser) userResponse.getKey(), userResponse.getValue()));
	    		}
	    		else
	    		{
	    			DataRequestGetFacebookUser getUser = new DataRequestGetFacebookUser(userResponse.getKey().getUserId());
	    			service.addDataRequest(getUser, new IRequestListener<AppUser>() {

						
						public void onRequestComplete(AppUser data) {
							UpdateList updater = new UpdateList(new UserUserStatus(data, userResponse.getValue()));
							UserInvites.this.runOnUiThread(updater);
						}

						
						public void onRequestFail(Exception e) {
							// TODO Auto-generated method stub
							
						}
	    				
	    			});
	    		}
	    	}
		}
		
		
		public int getCount() {
			return mylist.size();
		}

		
		public Object getItem(int position) {
			return mylist.get(position);
		}

		
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ViewGroup p = parent;            
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.user_row, p, false);
			}

			
			TextView userName = (TextView) v.findViewById(R.id.user_guest);
			userName.setText(mylist.get(position).user.toString());
			
			TextView freeFromText = (TextView) v.findViewById(R.id.user_time);
			UserStatus userStatus =  mylist.get(position).status;
			switch(userStatus.goingStatus)
			{
			case notGoing :
			{
				freeFromText.setText("Nah");
				break;
			}
			case going :
			{
				if(userStatus.freeFrom.equals(event.GetStartTime()) || userStatus.freeFrom.before(event.GetStartTime()))
				{
					//The user has either said this time or an earlier time and hence is free
					freeFromText.setText("Up for it");
				}
				else
				{
					freeFromText.setText(PubEvent.GetFormattedDate(userStatus.freeFrom));
				}
				break;
			}
			case maybeGoing : {
				freeFromText.setText("");
				break;
			}
			}
			return v;
		}
		
		class UpdateList implements Runnable
		{
			private UserUserStatus data;
			public UpdateList(UserUserStatus data)
			{
				this.data = data;
			}
			
			
			public void run() {
				// TODO Auto-generated method stub
				mylist.add(data);
				GuestAdapter.this.notifyDataSetChanged();
			}
			
		}
		
		class UserUserStatus
		{
			public AppUser user;
			public UserStatus status;
			
			public UserUserStatus(AppUser user, UserStatus status)
			{
				this.user = user;
				this.status = status;
			}
		}
	}
	
	
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}
}
package dimappers.android.pub;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.ResponseData;

public class UserInvites extends EventScreen implements OnLongClickListener 
{	

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.user_invites);
		Typeface font = Typeface.createFromAsset(getAssets(), "SkratchedUpOne.ttf");
		
    	Button button_going = (Button)findViewById(R.id.going);
    	button_going.setOnClickListener(this);
    	
    	Button button_long_click_going = (Button)findViewById(R.id.going);
    	button_long_click_going.setOnLongClickListener(this);
    	
    	Button button_decline = (Button) findViewById(R.id.decline);
    	button_decline.setOnClickListener(this);
    	
    	((TextView)findViewById(R.id.PubName)).setTypeface(font);
    	((TextView)findViewById(R.id.StartTime)).setTypeface(font);
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
			showCommentDialog();
			return true;
		}
		}
		return false;
	}
	
	TextView timeText;
	
	private void showCommentDialog() 
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
			if(requestCode==Constants.MessageTimeSetter)
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
	}
	
	public UserInvites()
	{
		super();
		connection = new UserInvitesEventServiceConnection();
	}
	
	private class UserInvitesEventServiceConnection extends EventServiceConnection
	{
		public void onServiceConnected(ComponentName name, IBinder serviceBinder)
		{
			super.onServiceConnected(name, serviceBinder);
			
			gAdapter = new UserInvitesGuestListAdapter(createAppUserList()); 
			setListAdapter(gAdapter);
			
			updateScreen();
		}
	};
	

	protected void updateScreen()
	{
		super.updateScreen();

    	service.GetAppUserFromUser(event.GetHost(), new IRequestListener<AppUser>() {
			
			public void onRequestFail(Exception e) {
				((TextView)findViewById(R.id.userInviteHostNameText)).setText(
																					getString(R.string.host_name)
																					+ " " +
																					"unknown");
				e.printStackTrace();
			}
			
			public void onRequestComplete(final AppUser data) {
				runOnUiThread(new Runnable() {
					
					public void run() {
						((TextView)findViewById(R.id.userInviteHostNameText)).setText(
																						getString(R.string.host_name)
																						+ " " +
																						data.toString());
					}
				});
			}
		});
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
						    			service.GetAppUserFromUser(event.GetHost(), new IRequestListener<AppUser>() {
											
											public void onRequestFail(Exception e) {
												Toast.makeText(
														getBaseContext(), 
														"Sent message \"" + msgToHost + "\" to host.", 
														Toast.LENGTH_LONG
														).show();
											}
											
											public void onRequestComplete(AppUser data) {
												Toast.makeText(
														getBaseContext(), 
														"Sent message \"" + msgToHost + "\" to " + data.toString() + ".", 
														Toast.LENGTH_LONG
														).show();
											}
										});
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
	
	class UserInvitesGuestListAdapter extends GeneralGuestListAdapter
	{
		List<AppUser> mylist;
		
		public UserInvitesGuestListAdapter(List<AppUser> users)
		{
			super(UserInvites.this, R.layout.user_row, R.id.guest, users);
			mylist = users;
		}
		
		class UpdateList implements Runnable
		{
			public void run() {
				UserInvitesGuestListAdapter.this.notifyDataSetChanged();
			}
		}
	}
	
	

}
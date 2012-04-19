package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
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
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;

public class UserInvites extends Activity implements OnClickListener, OnLongClickListener 
{

	PubEvent event;
	AppUser facebookUser;
	
	IPubService service;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		bindService(new Intent(this, PubService.class), connection, 0);
		
		setContentView(R.layout.user_invites);
		    	
    	Button button_going = (Button)findViewById(R.id.going);
    	button_going.setOnClickListener(this);
    	
    	Button button_long_click_going = (Button)findViewById(R.id.going);
    	button_long_click_going.setOnLongClickListener(this);
    	
    	Button button_decline = (Button) findViewById(R.id.decline);
    	button_decline.setOnClickListener(this);
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
		

		Button attachButton = (Button) commentDialog.findViewById(R.id.attach); 
		Button cancelButton = (Button) commentDialog.findViewById(R.id.cancel); 

		attachButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) 
		{ 
		
	    TextView text = (TextView) commentDialog.findViewById(R.id.comment_text_box);
	    TextView time = (TextView) commentDialog.findViewById(R.id.changeTime);
					
		String commentMade = text.getText().toString();
		Calendar timeChange = event.GetStartTime(); //(Calendar) time.getText();
					
		Toast.makeText(getBaseContext(), commentMade, Toast.LENGTH_LONG).show(); 
		
		sendResponse(true,timeChange,commentMade);
		commentDialog.dismiss();
		} 
		}); 

		cancelButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) 
		{ 
		commentDialog.dismiss(); 
		} 
		});
		
		commentDialog.show();
	}
	
	private ServiceConnection connection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder)
		{
			service = (IPubService)serviceBinder;
			
			event = service.getEvent(getIntent().getExtras().getInt(Constants.CurrentWorkingEvent));
			
			TextView pubNameText = (TextView) findViewById(R.id.userInvitesPubNameText);
	    	pubNameText.setText(event.GetPubLocation().toString());
	    	
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
			
			facebookUser = service.GetActiveUser();
			
			ListView list = (ListView) findViewById(R.id.listView2);   
			list.setAdapter(new GuestAdapter(event, service));
		}

		public void onServiceDisconnected(ComponentName arg0)
		{			
		}
		
	};
	
	//TODO: Connect this method to button presses
	private void sendResponse(boolean going, Calendar freeFromWhen, String msgToHost)
	{
		DataRequestSendResponse response = new DataRequestSendResponse(going, event.GetEventId(), freeFromWhen, msgToHost);
		service.addDataRequest(response, new IRequestListener<PubEvent>() {
					public void onRequestComplete(PubEvent data) {
						if(data != null)
						{
							event = data;
							//TODO: Update screen as we have received new information
						}
					}

					public void onRequestFail(Exception e) {
						Log.d(Constants.MsgError, e.getMessage());						
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
			for(final Entry<User, UserStatus> userResponse : event.GetGoingStatusMap().entrySet())
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
			freeFromText.setText(PubEvent.GetFormattedDate(mylist.get(position).status.freeFrom));
			
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
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}
}
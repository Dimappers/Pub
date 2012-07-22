package dimappers.android.pub;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public abstract class EventScreen extends ListActivity implements OnClickListener,  OnMenuItemClickListener
{
	
	protected PubEvent event;
	protected IPubService service;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		bindService(new Intent(this, PubService.class), connection, 0);
	}
	
	protected List<AppUser> createAppUserList()
	{
		User[] array = event.GetUserArray();
		List<AppUser> list = new ArrayList<AppUser>(array.length);
		for(int i=0; i < array.length; i++)
		{
			try {
				list.add(AppUser.AppUserFromUser(array[i], service.GetFacebook()));
			} catch (Exception e1) {
				e1.printStackTrace();
				Log.d(Constants.MsgError, "Error creating AppUser for User " + array[i].getUserId());
			}
		}
		return list;
	}
	
	protected void updateScreen()
	{
		
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}
	
	//NOTE: this is currently only acting as a placeholder
	protected EventServiceConnection connection;
	
	protected abstract class EventServiceConnection implements ServiceConnection
	{

		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			
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
					e.printStackTrace();
					Log.d(Constants.MsgError, "Error when refreshing event: " + e.getMessage());
				}

				
				public void onRequestComplete(PubEvent data) {
					event = data;
					runOnUiThread(new Runnable(){

						
						public void run() {
							updateScreen();
						}});
				}});
		}

		public void onServiceDisconnected(ComponentName name) {}
		
	}
	
 	public class GeneralGuestListAdapter extends ArrayAdapter<AppUser> 
	{

		public GeneralGuestListAdapter(Context context, int resource, int textViewResourceId, AppUser[] objects)
		{
			super(context, resource, textViewResourceId, objects);
		}

		public GeneralGuestListAdapter(Context context, int resource, int textViewResourceId, List<AppUser> objects)
		{
			super(context, resource, textViewResourceId, objects);
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			convertView = super.getView(position, convertView, parent);

			TextView freeFromText = (TextView) convertView.findViewById(R.id.time);
			
			UserStatus userStatus =  event.GetGoingStatusMap().get(getItem(position));
			
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
						freeFromText.setText("Up for it!");
					}
					else
					{
						freeFromText.setText(PubEvent.GetFormattedDate(userStatus.freeFrom));
					}
					break;
				}
				case maybeGoing : 
				{
					freeFromText.setText("");
					break;
				}
			}
			
			return convertView;
		}

	}
}

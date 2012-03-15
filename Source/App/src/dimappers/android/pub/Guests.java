package dimappers.android.pub;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

public class Guests extends ListActivity implements OnClickListener{
	ArrayList<AppUser> listItems=new ArrayList<AppUser>();
	ArrayAdapter<AppUser> adapter;
	ListView guest_list;
	PubEvent event;
	
	User[] allFriends;
	
	IPubService service;
	
	Facebook facebook;
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guests);
    	
    	Bundle bundle = getIntent().getExtras();
    	event = (PubEvent) bundle.getSerializable(Constants.CurrentWorkingEvent);
    	
    	if(event == null)
    	{
    		Log.d(Constants.MsgError, "Error finding pub data in Guests");
    		setResult(Constants.MissingDataInBundle);
    		finish();
    	}

		bindService(new Intent(this, PubService.class), connection, 0);
    	
		//until search is implemented
		findViewById(R.id.search_friends).setVisibility(View.INVISIBLE);
    	
    	guest_list = (ListView)findViewById(android.R.id.list);
    	guest_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		adapter = new ArrayAdapter<AppUser>(this, android.R.layout.simple_list_item_multiple_choice, listItems);
		setListAdapter(adapter);

    	Button save = (Button)findViewById(R.id.save);
    	save.setOnClickListener(this);
	}
	
	public void onResume(View v){
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	public void onClick(View v){
		Intent i;
		switch(v.getId())
		{
		case R.id.save : {
			Bundle b = new Bundle();
			b.putSerializable(Constants.CurrentWorkingEvent,event);
			Intent returnIntent = new Intent();
			returnIntent.putExtras(b);
			this.setResult(RESULT_OK,returnIntent);
			
			finish();
			break;
		}
		}
	}
	
	public void onListItemClick(ListView l, View v, int pos, long id) 
	{
		super.onListItemClick(l, v, pos, id);
		
		//Add/Remove guest from
		User modifedUser = listItems.get(pos);
		if(event.DoesContainUser(modifedUser))
		{
			event.RemoveUser(modifedUser);		}
		else
		{
			event.AddUser(modifedUser);
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
	}
	
	private void UpdateListView(AppUser[] sortedUsers)
	{
		runOnUiThread(new UpdateList(sortedUsers));
	}
	
	/*private void GetUsers()
	{
		if(!Constants.emulator)
		{
			/*JSONObject mefriends = null;
	    	try {
				mefriends = new JSONObject(facebook.request("me/friends"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	AppUser[] friends;
	    	try {
				JSONArray jasonsFriends = mefriends.getJSONArray("data");
				friends = new AppUser[jasonsFriends.length()];
				for (int i=0; i < jasonsFriends.length(); i++)
				{
					JSONObject jason = (JSONObject) jasonsFriends.get(i);
					friends[i] = new AppUser(Long.parseLong(jason.getString("id")), jason.getString("name"));
				
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d(Constants.MsgError, "JASON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			return friends;
			
			
		}
		else
		{
			return new AppUser[]{new AppUser(123L, "Made1"), new AppUser(242L, "Made2")};
		}
	}*/
	
	/*private AppUser[] GetSortedUsers()
	{
		PubEvent currentEvent = event;
		allFriends = new PersonRanker(currentEvent, service, allFriends).getArrayOfRankedFriends();
		AppUser[] appFriends = new AppUser[allFriends.length];
		for(int i = 0; i<appFriends.length; i++)
		{
			appFriends[i] = AppUser.AppUserFromUser(allFriends[i], facebook);
		}
		return appFriends;
	}*/
	
	private ServiceConnection connection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder bService)
		{
			//Give the interface to the app
			service = (IPubService)bService;
			if(!Constants.emulator)
			{
				facebook = service.GetFacebook();
				
				DataRequestGetFriends getFriends = new DataRequestGetFriends();
				service.addDataRequest(getFriends, new IRequestListener<AppUserArray>() {
	
					public void onRequestComplete(AppUserArray data) {
						UpdateListView(data.getArray());
					}
	
					public void onRequestFail(Exception e) {
						Log.d(Constants.MsgError, "Error getting friends: " + e.getMessage());
						finish();
					}
				});
			}
			else
			{
				UpdateListView(new AppUser[] { new AppUser(12L, "Test1"), new AppUser(41L, "Test2") } );
			}
		}

		public void onServiceDisconnected(ComponentName className)
		{
		}
		
	};
	
	class UpdateList implements Runnable
	{
		AppUser[] sortedArray; 
		public UpdateList(AppUser[] sortedArray)
		{
			this.sortedArray = sortedArray;
		}

		public void run() {
			listItems.clear();
	    	
			for(AppUser user : sortedArray) {
	    		listItems.add(user);
	    	}
			
			//Tick already selected guests
			for(int i  = 0; i < guest_list.getCount(); ++i)
			{
				User listUser = listItems.get(i);
				guest_list.setItemChecked(i, event.DoesContainUser(listUser));
			}
			
			adapter.notifyDataSetChanged();			
		}
	}
	/*
	private void doFacebookCall() {
		JSONObject friends = null;
		try {
			friends = new JSONObject(facebook.request("me/friends"));
			JSONArray jasonsFriends = friends.getJSONArray("data");
			allFriends = new User[jasonsFriends.length()];
			for (int i=0; i < jasonsFriends.length(); i++)
			{
				JSONObject jason = (JSONObject) jasonsFriends.get(i);
				allFriends[i] = new AppUser(Long.parseLong(jason.getString("id")), jason.getString("name"));
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
}

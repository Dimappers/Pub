package dimappers.android.pub;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

public class Guests extends ListActivity implements OnClickListener{
	ArrayList<AppUser> listItems=new ArrayList<AppUser>();
	ArrayAdapter<AppUser> adapter;
	ListView guest_list;
	PubEvent event;
	PubEvent eventPreEdit;
	
	boolean isSent;
	
	AppUser[] allFriends;
	
	IPubService service;
	
	Facebook facebook;
	
	private boolean searching = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guests);
		bindService(new Intent(this, PubService.class), connection, 0);
    	
		//until search is implemented
		findViewById(R.id.search_friends).setVisibility(View.INVISIBLE);
    	
    	guest_list = (ListView)findViewById(android.R.id.list);
    	guest_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	
    	guest_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				AppUser user = (AppUser)arg0.getItemAtPosition(arg2);
				Intent i = new Intent(getBaseContext(), RankBreakDown.class);
				i.putExtra("person", user);
				startActivity(i);
				
				return true;
			}
		});
		
		adapter = new ArrayAdapter<AppUser>(this, android.R.layout.simple_list_item_multiple_choice, listItems);
		setListAdapter(adapter);
		
    	Button save = (Button)findViewById(R.id.save);
    	save.setOnClickListener(this);
    	

	}
	
	@Override
	public boolean onSearchRequested()
	{
		searching = true;
		TextView searchBox = (TextView)findViewById(R.id.search_friends);
		searchBox.setText("");
		searchBox.setVisibility(View.VISIBLE);
		searchBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {

		    @Override
			public void onFocusChange(View v, boolean hasFocus) {

		        if (hasFocus) {
		                    Guests.this.getWindow().setSoftInputMode(

		                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		        }

		    }

		});
		searchBox.requestFocus();
		searchBox.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence searchTerm, int start, int before, int count) {
				searchTerm = searchTerm.toString().toLowerCase();
				if(searchTerm.equals(""))
				{
					UpdateListView(allFriends);
				}
				else
				{
					ArrayList<AppUser> usersToKeep = new ArrayList<AppUser>();
					for(AppUser user : allFriends)
					{
						if(user.toString().toLowerCase().contains(searchTerm))
						{
							usersToKeep.add(user);
						}
					}
					AppUser[] array = new AppUser[usersToKeep.size()];
					UpdateListView(usersToKeep.toArray(array));
					
					//Experimental feature: auto select if just one user
					if(usersToKeep.size() == 1)
					{
						if(!guest_list.isItemChecked(0))
						{
							guest_list.setItemChecked(0, true);
							AppUser newlyAddedUser = usersToKeep.get(0); 
							event.AddUser(newlyAddedUser);
						}
					}
				}				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		return true;
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        if(searching)
	        {
	        	TextView searchBox = (TextView)findViewById(R.id.search_friends);
	    		searchBox.setVisibility(View.INVISIBLE);
	    		UpdateListView(allFriends);
	        	return true;	
	        }
	        else
	        {
	        	return super.onKeyDown(keyCode, event);
	        }
	    }

	    return super.onKeyDown(keyCode, event);
	}

	
	public void onResume(View v){
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onClick(View v){
		switch(v.getId())
		{
		case R.id.save : {
			Bundle b = new Bundle();
			b.putInt(Constants.CurrentWorkingEvent, event.GetEventId());
			Intent returnIntent = new Intent();
			returnIntent.putExtras(b);
			this.setResult(RESULT_OK,returnIntent);
			DataRequestGetFriends.UpdateOrdering(allFriends, service);
			finish();
			break;
		}
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) 
	{	
		//Add/Remove guest from
		AppUser modifiedUser = listItems.get(pos);
		if(event.DoesContainUser(modifiedUser))
		{
			if(isSent&&eventPreEdit.DoesContainUser(modifiedUser))
			{
				guest_list.setItemChecked(pos, true);
				Toast.makeText(getApplicationContext(), "Cannot uninvite a guest!", Toast.LENGTH_LONG).show();
			}
			else
			{
					event.RemoveUser(modifiedUser);
			}
		}
		else
		{
			event.AddUser(modifiedUser);
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
	
	private ServiceConnection connection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName className, IBinder bService)
		{
			//Give the interface to the app
			service = (IPubService)bService;
			int eventId = getIntent().getExtras().getInt(Constants.CurrentWorkingEvent);
			event =  service.getEvent(eventId);
			eventPreEdit = new PubEvent(event.writeXml());
			facebook = service.GetFacebook();
			
			if(eventId>=0) {isSent = true;}
			else {isSent = false;}
			
			DataRequestGetFriends getFriends = new DataRequestGetFriends(getApplicationContext());
			service.addDataRequest(getFriends, new IRequestListener<AppUserArray>() {

				@Override
				public void onRequestComplete(AppUserArray data) {
					allFriends = data.getArray();
					UpdateListView(data.getArray());
				}

				@Override
				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, "Error getting friends: " + e.getMessage());
					finish();
				}
			});
		}

		@Override
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

		@Override
		public void run() {
			listItems.clear();
	    	
			ArrayList<AppUser> checkedUsers = new ArrayList<AppUser>();
			ArrayList<AppUser> uncheckedUsers = new ArrayList<AppUser>();
			
			
			for(AppUser user : sortedArray) {
	    		if(event.DoesContainUser(user))
	    		{
	    			checkedUsers.add(user);
	    		}
	    		else
	    		{
	    			uncheckedUsers.add(user);
	    		}
	    	}
			
			int i = 0;
			for(AppUser checkedUser: checkedUsers)
			{
				listItems.add(checkedUser);
				guest_list.setItemChecked(i, true);
				++i;
			}
			for(AppUser uncheckedUser : uncheckedUsers)
			{
				listItems.add(uncheckedUser);
				guest_list.setItemChecked(i, false);
				++i;
			}
			
			adapter.notifyDataSetChanged();			
		}
	}
}

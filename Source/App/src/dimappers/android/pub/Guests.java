package dimappers.android.pub;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

public class Guests extends ListActivity implements OnClickListener{
	ArrayList<AppUser> listItems=new ArrayList<AppUser>();
	ArrayAdapter<AppUser> adapter;
	ListView guest_list;
	PubEvent event;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guests);
    	
    	Bundle bundle = getIntent().getExtras();
    	event = (PubEvent) bundle.getSerializable(Constants.CurrentWorkingEvent);
    	
    	if(event == null)
    	{
    		Toast.makeText(getApplicationContext(), "Error finding pub data - please restart", 100).show();
    		setResult(Constants.MissingDataInBundle);
    		finish();
    	}
    
    	UpdateListView();
    	
    	
    	guest_list = (ListView)findViewById(android.R.id.list);
    	guest_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		adapter = new ArrayAdapter<AppUser>(this, android.R.layout.simple_list_item_multiple_choice, listItems);
		setListAdapter(adapter);
		
		//Tick already selected guests
		for(int i  = 0; i < guest_list.getCount(); ++i)
		{
			User listUser = listItems.get(i);
			guest_list.setItemChecked(i, event.DoesContainUser(listUser));
		}
		
    	
    	Button button_add_guest = (Button)findViewById(R.id.add_guest);
    	button_add_guest.setOnClickListener(this);
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
		case R.id.add_guest : {
			i = new Intent(this, ChooseGuest.class);
			startActivity(i);
			break;
		}
		}
	}
	public void onContentChange() 
	{
		super.onContentChanged();
	}
	
	public void onListItemClick(ListView l, View v, int pos, long id) 
	{
		super.onListItemClick(l, v, pos, id);
		
		//Add/Remove guest from
		AppUser modifedUser = listItems.get(pos);
		if(event.DoesContainUser(modifedUser))
		{
			event.RemoveUser(modifedUser);		}
		else
		{
			event.AddUser(modifedUser);
		}
	}
	
	private void UpdateListView()
	{
		listItems.clear();
    	
		for(User user : GetSortedUsers()) {
    		listItems.add(((AppUser) user));
    	}
	}
	
	private AppUser[] GetUsers()
	{
		//TODO: Generate a list of all facebook friends 
		
		return new AppUser[]{ new AppUser(12), new AppUser(124), new AppUser(1238), new AppUser(143), new AppUser(12341), new AppUser(14) };
	}
	
	private AppUser[] GetSortedUsers()
	{
		//TODO: Should go through the users, put users that are selected first, then recommended users and finally by alphabet
		return GetUsers();
	}
}

package dimappers.android.pub;

import java.util.ArrayList;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class Guests extends ListActivity implements OnClickListener{
	private static final int RESULT_ERROR = -1;
	ArrayList<AppUser> listItems=new ArrayList<AppUser>();
	ArrayAdapter<AppUser> adapter;
	ListView guest_list;
	PubEvent event;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guests);
    	
    	Bundle bundle = getIntent().getExtras();
    	event = (PubEvent) bundle.getSerializable("event");
    	
    	if(event == null)
    	{
    		Toast.makeText(getApplicationContext(), "Error finding pub data - please restart", 100).show();
    		setResult(RESULT_ERROR);
    		finish();
    	}
    
    	UpdateListView();
    	
    	//TODO: When not just "Test Guest", need to have different checkboxes & cases in the switch for each
    	//Could extend onClickListener for each guest in this class & extend classes within constructors for add_guest/save buttons
    	
    	guest_list = (ListView)findViewById(android.R.id.list);
		guest_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		adapter = new ArrayAdapter<AppUser>(this, android.R.layout.simple_list_item_multiple_choice, listItems);
		setListAdapter(adapter);
		
		//Tick already selected guests
		//This is O(n^2) probably need a better way of doing this
		int count = guest_list.getCount();
		for(int i  = 0; i < guest_list.getCount(); ++i)
		{
			boolean checked = false;
			User listUser = listItems.get(i);
			for(User user : event.GetUsers())
			{
				if(user.isEqual(listUser))
				{
					checked = true;
					break;
				}
			}
			/*CheckedTextView checkbox = (CheckedTextView)guest_list.geti);
			checkbox.setChecked(checked);*/
			guest_list.setItemChecked(i, checked);
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
		
		if(event.GetUsers().contains(modifedUser))
		{
			event.GetUsers().remove(modifedUser);
		}
		else
		{
			event.GetUsers().add(modifedUser);
		}
	}
	
	private void UpdateListView()
	{
		listItems.clear();
    	
		for(User user : GetSortedUsers()) {
    		listItems.add(((AppUser) user));
    	}
	}
	
	public void UpdateEventFromList()
	{
		
	}
	
	private AppUser[] GetUsers()
	{
		//TODO: Generate a list of all facebook friends 
		
		return new AppUser[]{ new AppUser(12), new AppUser(124), new AppUser(1238), new AppUser(143), new AppUser(12341) };
	}
	
	private AppUser[] GetSortedUsers()
	{
		//TODO: Should go through the users, put users that are selected first, then recommended users and finally by alphabet
		return GetUsers();
	}
}

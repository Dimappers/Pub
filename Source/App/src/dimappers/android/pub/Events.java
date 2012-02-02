package dimappers.android.pub;

import java.util.Calendar;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

public class Events extends ExpandableListActivity {
	
    ExpandableListAdapter mAdapter;
	
    int facebookId;
    
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);
		
		facebookId = getIntent().getExtras().getInt(Constants.CurrentFacebookUser);
		
		mAdapter = new EventListAdapter(this);
        setListAdapter(mAdapter);
    	
         ExpandableListView expview = (ExpandableListView) findViewById(android.R.id.list);
		 expview.setOnChildClickListener(this);
        
		 
	}

	 public boolean onChildClick(ExpandableListView parent,View v, int groupPosition, int childPosition, long id) 
	 {
		 Intent i;
		 groupPosition = (int) mAdapter.getGroupId(groupPosition);
		 childPosition = (int) mAdapter.getChildId(groupPosition, childPosition);
		 
		 switch(groupPosition)
		 {
			 case 0: 
			 {
				 	//PubEvent sent_event = new PubEvent(new Calendar(), new User(new Integer(1)));
					
					//Bundle bundle = new Bundle();
					//bundle.putSerializable("sent_event", sent_event);
					//bundle.putInt("test", 1992);
					i = new Intent(this, HostEvents.class);
					//i.putExtras(bundle);
					startActivity(i);
					return true;
			 }
			 case 1 :
			 {		
			 		
				 	i = new Intent(this, HostEvents.class);
					startActivity(i);
					return true;
			 }
			 case 2 : 
			 {
					i = new Intent(this, UserInvites.class);
					startActivity(i);
					return true;   
		     } 
			 case 3 :
			 {
			 		i = new Intent(this, UserInvites.class);
					startActivity(i);
					return true;
			 }
			 }
			 return false; 
	 }
	 
	 private PubEvent[] GetEvents()
	 {
		Calendar time1 = Calendar.getInstance();
		time1.set(Calendar.HOUR_OF_DAY, 18);
		
		Calendar time2 = Calendar.getInstance();
		time2.set(Calendar.HOUR_OF_DAY, 22);
		return new PubEvent[] { new PubEvent(time1, new PubLocation(10,10,"Spoons"), 
				new User(14)), new PubEvent(time2, new PubLocation(10,10,"Robins Wells"), new User(0)) } ; 
	 }
	 
	 
	 
}

class EventListAdapter extends BaseExpandableListAdapter {
	
    private String[] groups = { "Hosting", "Send Invites", "Waiting For Response", "Going" };
    private String[][] children = {getHostedEvents(),getSendInvites(),getWaitingForResponse(), getGoing()};

private String[] getHostedEvents()
{
	return new String[] {"Hosting1","Hosting2"};
}

private String[] getSendInvites()
{
	return new String[] {"Sending"};
}

private String[] getWaitingForResponse()
{
	return new String[] {"Waiting"};
}

private String[] getGoing()
{
	return new String[] {"Going"};
}

private Context context;

public EventListAdapter(Context context) {
    this.context = context;
}


public Object getChild(int groupPosition, int childPosition) {
	return children[groupPosition][childPosition];
}


public long getChildId(int groupPosition, int childPosition) {
	return childPosition;
}

public View getChildView(int groupPosition, int childPosition, boolean isLastChild,View convertView, ViewGroup parent) {
    TextView textView = getGenericView();
    textView.setText(getChild(groupPosition, childPosition).toString());
    return textView;
}

public TextView getGenericView() {
    // Layout parameters for the ExpandableListView
    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 64);
    TextView textView = new TextView(context);
    textView.setLayoutParams(lp);
    // Center the text vertically
    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    // Set the text starting position
    textView.setPadding(36, 0, 0, 0);
    return textView;
}


public int getChildrenCount(int groupPosition) {
	return children[groupPosition].length;
}


public Object getGroup(int groupPosition) {
	return groups[groupPosition];
}


public int getGroupCount() {
	return groups.length;
}


public long getGroupId(int groupPosition) {
	return groupPosition;
}

public View getGroupView(int groupPosition, boolean isExpanded, View convertView,ViewGroup parent) 
{
    TextView textView = getGenericView();
    textView.setText(getGroup(groupPosition).toString());
    return textView;
}

public boolean hasStableIds() {
	return true;
}

public boolean isChildSelectable(int groupPosition, int childPosition) {
	return true;
}

}

        
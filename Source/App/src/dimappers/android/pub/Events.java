package dimappers.android.pub;

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
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);
		
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
					i = new Intent(this, HostingEvents.class);
					startActivity(i);
					return true;
			 }
			 case 1 :
			 {		
			 		
				 	i = new Intent(this, SendInvites.class);
					startActivity(i);
					return true;
			 }
			 case 2 : 
			 {
					i = new Intent(this, ToSend.class);
					startActivity(i);
					return true;   
		     } 
			 case 3 :
			 {
			 		i = new Intent(this, Going.class);
					startActivity(i);
					return true;
			 }
			 }
			 return false; 
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

@Override
public Object getChild(int groupPosition, int childPosition) {
	return children[groupPosition][childPosition];
}

@Override
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

@Override
public int getChildrenCount(int groupPosition) {
	return children[groupPosition].length;
}

@Override
public Object getGroup(int groupPosition) {
	return groups[groupPosition];
}

@Override
public int getGroupCount() {
	return groups.length;
}

@Override
public long getGroupId(int groupPosition) {
	return groupPosition;
}

public View getGroupView(int groupPosition, boolean isExpanded, View convertView,ViewGroup parent) 
{
    TextView textView = getGenericView();
    textView.setText(getGroup(groupPosition).toString());
    return textView;
}


@Override
public boolean hasStableIds() {
	return true;
}

@Override
public boolean isChildSelectable(int groupPosition, int childPosition) {
	return true;
}

}

        
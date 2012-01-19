package dimappers.android.pub;

import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;


public class Events extends ExpandableListActivity {
	
    ExpandableListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);
		Toast.makeText(getApplicationContext(), "hello jason", Toast.LENGTH_LONG).show();
		
		mAdapter = new EventListAdapter();
        setListAdapter(mAdapter);
        registerForContextMenu(getExpandableListView());	
	}

	

}


class EventListAdapter extends BaseExpandableListAdapter {
	
    private String[] groups = { "Hosting", "Send Invites", "Waiting For Response", "Going" };
    private String[][] children = {getHostedEvents(),getSendInvites(),getWaitingForResponse(), getGoing()};

private String[] getHostedEvents()
{
	return new String[] {"Hosting"};
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

@Override
public Object getChild(int arg0, int arg1) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public long getChildId(int arg0, int arg1) {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
		ViewGroup arg4) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public int getChildrenCount(int arg0) {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public Object getGroup(int arg0) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public int getGroupCount() {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public long getGroupId(int arg0) {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public boolean hasStableIds() {
	// TODO Auto-generated method stub
	return false;
}

@Override
public boolean isChildSelectable(int arg0, int arg1) {
	// TODO Auto-generated method stub
	return false;
}


    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
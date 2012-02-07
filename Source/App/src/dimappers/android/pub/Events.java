package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;

import dimappers.android.PubData.PubLocation;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.User;

public class Events extends ExpandableListActivity {

	ExpandableListAdapter mAdapter;

	AppUser facebookUser;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);

		facebookUser = (AppUser)getIntent().getExtras().getSerializable(Constants.CurrentFacebookUser);

		mAdapter = new EventListAdapter(this, GetEvents(), facebookUser);
		setListAdapter(mAdapter);

		ExpandableListView expview = (ExpandableListView) findViewById(android.R.id.list);
		expview.setOnChildClickListener(this);
	}

	public boolean onChildClick(ExpandableListView parent,View v, int groupPosition, int childPosition, long id) 
	{
		Intent i;
		groupPosition = (int) mAdapter.getGroupId(groupPosition);
		childPosition = (int) mAdapter.getChildId(groupPosition, childPosition);

		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.CurrentWorkingEvent, (PubEvent)mAdapter.getChild(groupPosition, childPosition));
		bundle.putAll(getIntent().getExtras());

		switch(groupPosition)
		{
			case Constants.ProposedEventNoResponse: 
			{
				i = new Intent(this, UserInvites.class);
				i.putExtras(bundle);
				startActivity(i);
				return true;
			}
			case Constants.HostedEventSent :
			{		
				i = new Intent(this, HostEvents.class);
				bundle.putBoolean(Constants.IsSavedEventFlag, false);
				i.putExtras(bundle);
				startActivity(i);
				return true;
			}
			case Constants.ProposedEventHaveResponded : 
			{
				i = new Intent(this, UserInvites.class);
				i.putExtras(bundle);
				startActivity(i);
				return true;   
			} 
			case Constants.HostedEventSaved :
			{
				i = new Intent(this, HostEvents.class);
				bundle.putBoolean(Constants.IsSavedEventFlag, true);
				i.putExtras(bundle);
				startActivity(i);
				return true;
			}
		}
		return false; 
	}

	private ArrayList<PubEvent> GetEvents()
	{
		ArrayList<PubEvent> events = new ArrayList<PubEvent>();
		
		StoredData storedData = StoredData.getInstance();
		events.addAll(storedData.GetAllEvents());
		
		
		Calendar time1 = Calendar.getInstance();
		time1.set(Calendar.HOUR_OF_DAY, 18);
		time1.add(Calendar.DAY_OF_MONTH, 1);

		Calendar time2 = Calendar.getInstance();
		time2.set(Calendar.HOUR_OF_DAY, 22);
		
		PubEvent hostedEvent = new PubEvent(time2, new PubLocation(10,10,"Spoons"), facebookUser);
		PubEvent invitedEvent = new PubEvent(time1, new PubLocation(10,10,"Robins Wells"), new User(123));
		
		invitedEvent.AddUser(new User(142));
		invitedEvent.AddUser(new User(42));
		invitedEvent.AddUser(new User(124));
		invitedEvent.AddUser(facebookUser); //add ourself to the event
		
		hostedEvent.AddUser(new User(1494));
		hostedEvent.AddUser(new User(123951));
		hostedEvent.SetEventId(1); //Pretend we have sent it to the server
		
		invitedEvent.UpdateUserStatus(new ResponseData(new User(42), 123, true));
		ResponseData anotherResponse = new ResponseData(new User(124), 123, true, time2, "Yeah busy till 10");
		invitedEvent.UpdateUserStatus(anotherResponse);
		
		//return new PubEvent[] {hostedEvent, invitedEvent } ;
		events.add(invitedEvent);
		events.add(hostedEvent);
		return events;
	}	 
}

class EventListAdapter extends BaseExpandableListAdapter {

	private final String[] groups = { "Waiting For Response", "Hosting", "Responded to", "Send Invites" };
	//ProposedEventNoResponse, HostedEventSent, ProposedEventResponded, HostedEventSaved

	//Cannot have array of generics in Java :(
	private ArrayList<PubEvent> waitingForResponse;
	private ArrayList<PubEvent> hosting;
	private ArrayList<PubEvent> respondedTo;
	private ArrayList<PubEvent> savedEvents;

	private Context context;

	public EventListAdapter(Context context, ArrayList<PubEvent> events, AppUser currentUser) {
		this.context = context;

		waitingForResponse = new ArrayList<PubEvent>();
		hosting = new ArrayList<PubEvent>();
		respondedTo = new ArrayList<PubEvent>();
		savedEvents = new ArrayList<PubEvent>();

		for(PubEvent event : events)
		{
			//Determine if host 
			if(event.GetHost().equals(currentUser))
			{
				//We are the host
				if(event.GetEventId() >= 0) //if the event has an id then it has been sent to the server
				{
					hosting.add(event);
				}
				else //if not then it is only storred locally
				{
					savedEvents.add(event);
				}
			}
			else
			{
				//We are not the host
				if(event.GetGoingStatus().get(currentUser).goingStatus == GoingStatus.maybeGoing) //we have not replied if status is still maybe
				{
					waitingForResponse.add(event);
				}
				else //otherwise we have replied with yes or no
				{
					respondedTo.add(event);
				}
			}
		}
	}


	public Object getChild(int groupPosition, int childPosition) {
		return GetRelevantList(groupPosition).get(childPosition);
	}

	private ArrayList<PubEvent> GetRelevantList(int groupPosition)
	{
		switch(groupPosition)
		{
			case Constants.HostedEventSaved:
				return savedEvents;

			case Constants.HostedEventSent:
				return hosting;

			case Constants.ProposedEventNoResponse:
				return waitingForResponse;

			case Constants.ProposedEventHaveResponded:
				return respondedTo;
		}

		Log.d(Constants.MsgError, "Attempted to get non-existant group on the events screen");
		return null;
	}

	public long getChildId(int groupPosition, int childPosition) {
		//TODO: not sure if this is correct - not sure if we use it either
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
		return GetRelevantList(groupPosition).size();
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


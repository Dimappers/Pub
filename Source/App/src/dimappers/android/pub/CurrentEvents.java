package dimappers.android.pub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.User;

public class CurrentEvents extends ListActivity implements OnItemClickListener 
{
	SeperatedListAdapter adapter;
	IPubService serviceInterface; 
	AppUser facebookUser;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_events);

		facebookUser = (AppUser)getIntent().getExtras().getSerializable(Constants.CurrentFacebookUser);
		bindService(new Intent(this, PubService.class), connection, 0);

		// Create the ListView Adapter
		adapter = new SeperatedListAdapter(this, facebookUser);

		adapter.addSection("Waiting For Response", new ArrayAdapter<PubEvent>(this,R.layout.list_item, adapter.waitingForResponse));  
		adapter.addSection("Hosting", new ArrayAdapter<PubEvent>(this,R.layout.list_item, adapter.hosting));  
		adapter.addSection("Responded To", new ArrayAdapter<PubEvent>(this,R.layout.list_item, adapter.respondedTo));  
		adapter.addSection("Send Invites", new ArrayAdapter<PubEvent>(this,R.layout.list_item, adapter.savedEvents));  


		ListView listview = (ListView) findViewById(android.R.id.list);

		setListAdapter(adapter);
		listview.setOnItemClickListener(this);

	}

	public void onItemClick(AdapterView<?> parent, View convertView, int position, long location) 
	{
		Intent i;
		Object section = adapter.getSection(position);
		int sectionnum = (int) adapter.getHeaderId(section, position);

		if(section == "Waiting For Response")
			position = position - 1;
		else if(section == "Hosting")
			position = position - 2 - adapter.waitingForResponse.size();
		else if(section == "Responded To")
			position = position - 3 - adapter.waitingForResponse.size() - adapter.hosting.size();
		else if(section == "Saved Events")
			position = position - 4 - adapter.waitingForResponse.size() - adapter.hosting.size() - adapter.respondedTo.size();


		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.CurrentWorkingEvent, (PubEvent)adapter.getHeader(sectionnum, position));
		bundle.putAll(getIntent().getExtras());		


		switch(sectionnum)
		{
		case Constants.ProposedEventNoResponse: 
		{
			i = new Intent(this, UserInvites.class);
			i.putExtras(bundle);
			startActivity(i);
			break;
		}
		case Constants.HostedEventSent :
		{		
			i = new Intent(this, HostEvents.class);
			bundle.putBoolean(Constants.IsSavedEventFlag, false);
			i.putExtras(bundle);
			startActivity(i);
			break;
		}
		case Constants.ProposedEventHaveResponded : 
		{
			i = new Intent(this, UserInvites.class);
			i.putExtras(bundle);
			startActivity(i);
			break;
		} 
		case Constants.HostedEventSaved :
		{
			i = new Intent(this, HostEvents.class);
			bundle.putBoolean(Constants.IsSavedEventFlag, true);
			i.putExtras(bundle);
			startActivity(i);
			break;
		}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	private ArrayList<PubEvent> GetEvents()
	{
		ArrayList<PubEvent> events = new ArrayList<PubEvent>();

		events.addAll(serviceInterface.GetSavedEvents());


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

	private ServiceConnection connection = new ServiceConnection()
	{

		public void onServiceConnected(ComponentName className, IBinder service)
		{
			//Give the interface to the app
			serviceInterface = (IPubService)service;
			((SeperatedListAdapter)adapter).setServiceInterface(serviceInterface);
			adapter.notifyDataSetChanged();
		}

		public void onServiceDisconnected(ComponentName className)
		{

		}

	};

}

class SeperatedListAdapter extends BaseAdapter
{

	public final Map<String,Adapter> sections = new LinkedHashMap<String,Adapter>();  
	public final ArrayAdapter<String> headers;  
	public final static int TYPE_SECTION_HEADER = 0;

	public ArrayList<PubEvent> waitingForResponse;
	public ArrayList<PubEvent> hosting;
	public ArrayList<PubEvent> respondedTo;
	public ArrayList<PubEvent> savedEvents;

	private IPubService serviceInterface;
	private Context context;
	private User currentUser;
	private ArrayList<PubEvent> events;

	public SeperatedListAdapter(Context context, AppUser currentUser) 
	{  
		this.context = context;
		this.currentUser = currentUser;

		headers = new ArrayAdapter<String>(context, R.layout.header);  
		waitingForResponse = new ArrayList<PubEvent>();
		hosting = new ArrayList<PubEvent>();
		respondedTo = new ArrayList<PubEvent>();
		savedEvents = new ArrayList<PubEvent>();


		/*for(PubEvent event : events)
		{
			//Determine if host 
			if(event.GetHost().equals(currentUser))
			{
				//We are the host
				if(event.GetEventId() >= 0) //if the event has an id then it has been sent to the server
				{
					hosting.add(event);
				}
				else //if not then it is only stored locally
				{
					savedEvents.add(event);
				}
			}
			else
			{
				//We are not the host
				if(event.GetUserGoingStatus(currentUser) == GoingStatus.maybeGoing) //we have not replied if status is still maybe	
				{
					waitingForResponse.add(event);
				}
				else //otherwise we have replied with yes or no
				{
					respondedTo.add(event);
				}
			}
		}*/
	}  

	public void setServiceInterface(IPubService serviceInterface)
	{
		this.serviceInterface = serviceInterface;
	}

	public Object getSection(int position) 
	{
		String section = "null";

		if(waitingForResponse.contains(getItem(position)) ==  true)
			section = "Waiting For Response";
		else if(hosting.contains(getItem(position)) ==  true)
			section = "Hosting";
		else if(respondedTo.contains(getItem(position)) ==  true)
			section = "Responded To";
		else if(savedEvents.contains(getItem(position)) ==  true)
			section = "Send Invites";

		return section;

	}

	public void addSection(String section, Adapter adapter) 
	{  
		this.headers.add(section);  
		this.sections.put(section, adapter);  
	}  

	public int getCount() 
	{
		// total together all sections, plus one for each section header  
		int total = 0;  
		for(Adapter adapter : this.sections.values())  
			total += adapter.getCount() + 1;  
		return total; 
	}

	public Object getItem(int position) 
	{
		for(Object section : this.sections.keySet()) 
		{  
			Adapter adapter = sections.get(section);  
			int size = adapter.getCount() + 1;  

			// check if position inside this section  
			if(position == 0) return section;  
			if(position < size) return adapter.getItem(position - 1);  

			// otherwise jump into next section  
			position -= size;  
		}  
		return null; 
	}

	public int getHeaderId(Object section, int position)
	{
		if(section == "Waiting For Response")
			position = 0;
		else if(section == "Hosting")
			position = 1;
		else if(section == "Responded To")
			position = 2;
		else if(section == "Send Invites")
			position = 3;

		return position;
	}

	public Object getHeader(int sectionnum, int position)
	{
		return (PubEvent)GetRelevantList(sectionnum).toArray()[position];
	}

	//Position is counting both headers and events, not just headers
	private Collection<PubEvent> GetRelevantList(int position)
	{
		//If we are still waiting on the service to bind, display no data (maybe with progress bar
		if(serviceInterface == null)
		{
			return new ArrayList<PubEvent>();
		}
		switch(position)
		{
		case Constants.HostedEventSaved:
			return serviceInterface.GetSavedEvents();

		case Constants.HostedEventSent:
			return serviceInterface.GetSentEvents();

		case Constants.ProposedEventNoResponse:
			ArrayList<PubEvent> noResponse = new ArrayList<PubEvent>();
			for(PubEvent event : serviceInterface.GetAllInvited())
			{
				if(event.GetUserGoingStatus(currentUser) == GoingStatus.maybeGoing)
				{
					noResponse.add(event);
				}
			}

			return noResponse;

		case Constants.ProposedEventHaveResponded:
			ArrayList<PubEvent> haveResponse = new ArrayList<PubEvent>();
			for(PubEvent event : serviceInterface.GetAllInvited())
			{
				//at the moment this list includes all responses, can change this for just going
				if(event.GetUserGoingStatus(currentUser) != GoingStatus.maybeGoing) 
				{
					haveResponse.add(event);
				}
			}

			return haveResponse;
		}

		Log.d(Constants.MsgError, "Attempted to get non-existant group on the events screen");
		return null;
	}


	public long getItemId(int position) 
	{
		return position;
	}


	public View getView(int position, View convertView, ViewGroup parent) 
	{
		int sectionnum = 0;  
		for(Object section : this.sections.keySet()) {  
			Adapter adapter = sections.get(section);  
			int size = adapter.getCount() + 1;  

			// check if position inside this section  
			if(position == 0) return headers.getView(sectionnum, convertView, parent);  
			if(position < size) return adapter.getView(position - 1, convertView, parent);  

			// otherwise jump into next section  
			position -= size;  
			sectionnum++;  
		}  


		return null;
	}

}

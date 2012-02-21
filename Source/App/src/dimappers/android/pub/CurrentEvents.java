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

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_events);

		AppUser facebookUser = (AppUser)getIntent().getExtras().getSerializable(Constants.CurrentFacebookUser);
		bindService(new Intent(this, PubService.class), connection, 0);

		// Create the ListView Adapter
		adapter = new SeperatedListAdapter(this,facebookUser);
		setListAdapter(adapter);

		((ListView)findViewById(android.R.id.list)).setOnItemClickListener(this);
	}
	
	@Override 
	public void onDestroy()
	{
		super.onDestroy();
		unbindService(connection);
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

	private ServiceConnection connection = new ServiceConnection()
	{

		public void onServiceConnected(ComponentName className, IBinder service)
		{
			//Give the interface to the app
			IPubService serviceInterface = (IPubService)service;
			((SeperatedListAdapter)adapter).setServiceInterface(serviceInterface);
			String loadData = CurrentEvents.this.getSharedPreferences(Constants.SaveDataName, MODE_PRIVATE).getString(Constants.SaveDataName, "NoSave");
			if(loadData != "NoSave")
			{
				serviceInterface.Load(loadData);
			}
			
			adapter.setData(serviceInterface);
			//adapter.notifyDataSetChanged();
		}

		public void onServiceDisconnected(ComponentName className)
		{

		}

	};

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

		public SeperatedListAdapter(Context context, AppUser currentUser) 
		{  
			this.context = context;
			this.currentUser = currentUser;

			headers = new ArrayAdapter<String>(context, R.layout.header);  
			waitingForResponse = new ArrayList<PubEvent>();
			hosting = new ArrayList<PubEvent>();
			respondedTo = new ArrayList<PubEvent>();
			savedEvents = new ArrayList<PubEvent>();
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

		public void setData(IPubService serviceInterface)
		{
			ArrayAdapter<PubEvent> hostingSaved = new ArrayAdapter<PubEvent>(CurrentEvents.this.getApplicationContext(), R.layout.list_item);
			for(PubEvent savedEvent : serviceInterface.GetSavedEvents())
			{
				hostingSaved.add(savedEvent);
			}
			
			ArrayAdapter<PubEvent> hostingSent = new ArrayAdapter<PubEvent>(CurrentEvents.this.getApplicationContext(), R.layout.list_item);
			for(PubEvent sentEvent : serviceInterface.GetSentEvents())
			{
				hostingSent.add(sentEvent);
			}
			
			ArrayAdapter<PubEvent> waitingForResponse = new ArrayAdapter<PubEvent>(CurrentEvents.this.getApplicationContext(), R.layout.list_item);
			ArrayAdapter<PubEvent> respondedTo = new ArrayAdapter<PubEvent>(CurrentEvents.this.getApplicationContext(), R.layout.list_item);
			
			for(PubEvent event : serviceInterface.GetInvitedEvents())
			{
				if(event.GetUserGoingStatus(currentUser) == GoingStatus.maybeGoing)
				{
					waitingForResponse.add(event);
				}
				else
				{
					respondedTo.add(event);
				}
			}
			
			//Keep in this order unless you want it to break!!! 
			addSection("Waiting for response", waitingForResponse);		
			addSection("Sent invites", hostingSent);
			addSection("Responded to", respondedTo);
			addSection("Saved Invites", hostingSaved);
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
			return GetRelevantList(sectionnum).get(position);
		}
		
		private ArrayList<PubEvent> GetRelevantList(int position)
		{
			switch(position)
			{
			case Constants.HostedEventSent:
				return hosting;
			}
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

}

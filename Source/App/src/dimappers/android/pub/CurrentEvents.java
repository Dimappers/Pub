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
	IPubService service;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_events);
		
		bindService(new Intent(this, PubService.class), connection, 0);

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
		int sectionnum = adapter.getItemCategory(position);

		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.CurrentWorkingEvent, (PubEvent)adapter.getItem(position));
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

	private ServiceConnection connection = new ServiceConnection()
	{

		public void onServiceConnected(ComponentName className, IBinder service)
		{
			//Give the interface to the app
			CurrentEvents.this.service = (IPubService)service;
			AppUser facebookUser = CurrentEvents.this.service.GetActiveUser();
			
			PubEvent createdEvent = (PubEvent) getIntent().getExtras().getSerializable(Constants.CurrentWorkingEvent); 
			if(createdEvent != null) {
				//Then skip straight in to the relevant next screen
				Intent i;
				Bundle b = new Bundle();
				b.putAll(getIntent().getExtras());
				
				if(createdEvent.GetHost().equals(facebookUser))
				{
					i = new Intent(CurrentEvents.this, HostEvents.class);					
				}
				else
				{
					i = new Intent(CurrentEvents.this, UserInvites.class);
				}
				
				i.putExtras(b);
				startActivity(i);
			}
			
			adapter = new SeperatedListAdapter(CurrentEvents.this,facebookUser);
			setListAdapter(adapter);
			
			adapter.setData(CurrentEvents.this.service);
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

		private User currentUser;

		public SeperatedListAdapter(Context context, AppUser facebookUser) 
		{  
			this.currentUser = facebookUser;

			headers = new ArrayAdapter<String>(context, R.layout.header);  
		}  

		//Read the data out of the service and put it in to the adapters
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
			
			ArrayAdapter<PubEvent> waitingForResponses = new ArrayAdapter<PubEvent>(CurrentEvents.this.getApplicationContext(), R.layout.list_item);
			ArrayAdapter<PubEvent> respondedto = new ArrayAdapter<PubEvent>(CurrentEvents.this.getApplicationContext(), R.layout.list_item);
			
			for(PubEvent event : serviceInterface.GetInvitedEvents())
			{
				if(event.GetUserGoingStatus(currentUser) == GoingStatus.maybeGoing)
				{
					waitingForResponses.add(event);
				}
				else
				{
					respondedto.add(event);
				}
			}
			
			//Keep in this order unless you want it to break!!! 
			addSection("Waiting for response", waitingForResponses);		
			addSection("Sent invites", hostingSent);
			addSection("Responded to", respondedto);
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

		//Gets the PubEvent from the position, counting through the lists
		public Object getItem(int position) {
			//Identify section in
			for(Adapter sectionAdapter : sections.values())
			{
				--position; //discount the header
			
				if(position < sectionAdapter.getCount()) // does this position fit in to this list?
				{
					return sectionAdapter.getItem(position);
				}
				else // no? ok move on to the next list discounting the length of this list
				{
					position -= sectionAdapter.getCount();
				}
			}
			Log.d(Constants.MsgError, "Couldn't find position");
			return null;	
		}
		
		//Gets the category the item is in
		public int getItemCategory(int position)
		{
			int i = 0;
			for(Adapter sectionAdapter : sections.values())
			{
				--position; //discount the header
			
				if(position < sectionAdapter.getCount())
				{
					return i;
				}
				else
				{
					position -= sectionAdapter.getCount();
					++i;
				}
			}
			Log.d(Constants.MsgError, "Couldn't find position");
			return -1;
		}	
	}
}

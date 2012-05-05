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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.User;
import dimappers.android.pub.HostEvents.TimeTillPub;

public class CurrentEvents extends ListActivity implements OnItemClickListener {
	SeperatedListAdapter adapter;
	IPubService service = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_events);

		bindService(new Intent(this, PubService.class), connection, 0);

		((ListView) findViewById(android.R.id.list))
				.setOnItemClickListener(this);
		
		registerForContextMenu(getListView());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem refreshBtn = menu.add("Refresh");
		refreshBtn.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem arg0) {
				DataRequestRefresh refreshRequest = new DataRequestRefresh(true);
				service.addDataRequest(refreshRequest,
						new IRequestListener<PubEventArray>() {

							public void onRequestComplete(PubEventArray data) {
								// TODO: Probably shouldn't make notifications
								if (data.getEvents().size() > 0) {
									CurrentEvents.this
											.runOnUiThread(new Runnable() {

												public void run() {
													refreshList();

												}
											});
								}
							}

							public void onRequestFail(Exception e) {
								Log.d(Constants.MsgError,
										"Error getting refresh: "
												+ e.getMessage());

							}
						});
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshList();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    int pos = ((AdapterContextMenuInfo)menuInfo).position;
	    pos = adapter.getItemCategory(pos); //Convert into a category
	    MenuInflater inflater = getMenuInflater();
	    switch (pos) {
			case Constants.ProposedEventNoResponse:
			case Constants.ProposedEventHaveResponded:{
				inflater.inflate(R.menu.invited_hold_menu, menu);
				break;
			}
			case Constants.HostedEventSent: {
				inflater.inflate(R.menu.host_sent_hold_menu, menu);
				break;
			}
			case Constants.HostedEventSaved: {
				inflater.inflate(R.menu.host_saved_hold_menu, menu);
				break;
			}
			}
	    
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    int itemPosition = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
	    Object pubEventO = adapter.getItem(itemPosition);
	    if(pubEventO != null && pubEventO instanceof PubEvent)
	    {
	    	PubEvent selectedEvent = (PubEvent)pubEventO;
		    switch (item.getItemId()) {
		    	
		    	//Saved event - click delete
		        case R.id.host_saved_menu_item_delete:
		            Log.d(Constants.MsgInfo, "Delete event");
		            service.RemoveEventFromStoredDataAndCancelNotification(selectedEvent);
		            refreshList();
		            return true;
		            
		        //Saved event - click send
		        case R.id.host_saved_menu_item_send:
		        	Log.d(Constants.MsgInfo, "Send event");
		        	service.GiveNewSentEvent(selectedEvent, new IRequestListener<PubEvent>() {
						
						public void onRequestFail(Exception e) {
							Log.d(Constants.MsgError, "Could not sent invite: " + e.getMessage());
							runOnUiThread(new Runnable(){
								public void run() {
									Toast.makeText(getApplicationContext(),"Unable to send event, please try again later.",Toast.LENGTH_LONG).show();
									//FIXME: probably should make it more obvious when this fails
								}});
						}
						
						public void onRequestComplete(PubEvent data) {
							Log.d(Constants.MsgInfo, "PubEvent sent, event id: " + data.GetEventId());
							CurrentEvents.this.runOnUiThread(new Runnable()
							{
								public void run() {
									refreshList();
								}
								
							});
						}
		        	});
		        	
		            return true;
		            
		        case R.id.host_saved_menu_item_edit:
		        case R.id.host_sent_menu_item_edit:
		        	Intent i;
		        	Bundle bundle = new Bundle();
					bundle.putInt(Constants.CurrentWorkingEvent, selectedEvent.GetEventId());
					bundle.putBoolean(Constants.IsSavedEventFlag, true);

					i = new Intent(this, Organise.class);
					i.putExtras(bundle);
					startActivityForResult(i, Constants.FromEdit);
		        	return true;
		            
		        //Sent event, confirming it is on
		        case R.id.host_sent_menu_item_confirm:
		        	selectedEvent.setCurrentStatus(EventStatus.itsOn);
					DataRequestConfirmDeny request = new DataRequestConfirmDeny(selectedEvent);
					service.addDataRequest(request, new IRequestListener<PubEvent>() {

						public void onRequestComplete(PubEvent data) {
							if(data != null)
							{
								CurrentEvents.this.runOnUiThread(new Runnable()
								{
									public void run() {
										refreshList();
									}
									
								});
							}
							
						}

						public void onRequestFail(Exception e) {
							Log.d(Constants.MsgError, e.getMessage());					
						}
					});
					refreshList();
		        	return true;
		        	
		        case R.id.host_sent_menu_item_cancel:
		        	service.CancelEvent(selectedEvent);
		        	refreshList();
		        	return true;
		        	
		        case R.id.invited_menu_item_up:
		        	DataRequestSendResponse response = new DataRequestSendResponse(true, selectedEvent.GetEventId(), selectedEvent.GetStartTime(), "");
		    		
		    		//Work around: we should get updated event back from the server and refresh from that 
		        	selectedEvent.UpdateUserStatus(new ResponseData(service.GetActiveUser(), selectedEvent.GetEventId(), true, selectedEvent.GetStartTime(), ""));
		    		refreshList();
		    		service.addDataRequest(response, new IRequestListener<PubEvent>() {
		    					public void onRequestComplete(PubEvent data) {
		    						runOnUiThread(new Runnable()
		    						{
										public void run()
										{
											refreshList();
										}
		    						});
		    					}

		    					public void onRequestFail(Exception e) {
		    						Log.d(Constants.MsgError, e.getMessage());						
		    					}
		    				});
		        	return true;
		        	
		        case R.id.invited_menu_item_no:
		        	DataRequestSendResponse negativeresponse = new DataRequestSendResponse(false, selectedEvent.GetEventId(), selectedEvent.GetStartTime(), "");
		    		
		    		//Work around: we should get updated event back from the server and refresh from that 
		        	selectedEvent.UpdateUserStatus(new ResponseData(service.GetActiveUser(), selectedEvent.GetEventId(), false, selectedEvent.GetStartTime(), ""));
		    		refreshList();
		    		service.addDataRequest(negativeresponse, new IRequestListener<PubEvent>() {
		    					public void onRequestComplete(PubEvent data) {
		    						runOnUiThread(new Runnable()
		    						{
										public void run()
										{
											refreshList();
										}
		    						});
		    					}

		    					public void onRequestFail(Exception e) {
		    						Log.d(Constants.MsgError, e.getMessage());						
		    					}
		    				});
		        	return true;
		        default:
		            return super.onContextItemSelected(item);
		    }
		    }
        return super.onContextItemSelected(item);
	    
	}

	private void refreshList() {
		// This is a bit hacky, recreate the entire list adapter just to update
		// the data, however, calling notifyDatasetInvalid seems to break things
		// 0 making headers items etc.
		// Ideally, we would resolve this, this is a work around
		if (service != null) {
			adapter = new SeperatedListAdapter(this, service.GetActiveUser());
			setListAdapter(adapter);

			adapter.setData(service);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(connection);
		service = null;
	}

	public void onItemClick(AdapterView<?> parent, View convertView,
			int position, long location) {
		Intent i;
		int sectionnum = adapter.getItemCategory(position);

		Bundle bundle = new Bundle();
		bundle.putInt(Constants.CurrentWorkingEvent,
				((PubEvent) adapter.getItem(position)).GetEventId());
		switch (sectionnum) {
		case Constants.ProposedEventNoResponse: {
			i = new Intent(this, UserInvites.class);
			i.putExtras(bundle);
			startActivityForResult(i, 0);
			break;
		}
		case Constants.HostedEventSent: {
			i = new Intent(this, HostEvents.class);
			bundle.putBoolean(Constants.IsSavedEventFlag, false);
			i.putExtras(bundle);
			startActivityForResult(i, 0);
			break;
		}
		case Constants.ProposedEventHaveResponded: {
			i = new Intent(this, UserInvites.class);
			i.putExtras(bundle);
			startActivityForResult(i,0);
			break;
		}
		case Constants.HostedEventSaved: {
			i = new Intent(this, HostEvents.class);
			bundle.putBoolean(Constants.IsSavedEventFlag, true);
			i.putExtras(bundle);
			startActivityForResult(i,0);
			break;
		}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent i)
	{
		refreshList();
	}

	private ServiceConnection connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// Give the interface to the app
			CurrentEvents.this.service = (IPubService) service;
			AppUser facebookUser = CurrentEvents.this.service.GetActiveUser();

			if (getIntent().getExtras() != null
					&& getIntent().getExtras().containsKey(
							Constants.CurrentWorkingEvent)) {
				// Then skip straight in to the relevant next screen
				PubEvent createdEvent = CurrentEvents.this.service
						.getEvent(getIntent().getExtras().getInt(
								Constants.CurrentWorkingEvent));
				Intent i;
				Bundle b = new Bundle();
				// FIXME: null pointers be here
				b.putInt(Constants.CurrentWorkingEvent,
						createdEvent.GetEventId());
				b.putBoolean(Constants.IsSavedEventFlag, getIntent()
						.getExtras().getBoolean(Constants.IsSavedEventFlag));
				if (createdEvent.GetHost().equals(facebookUser)) {
					i = new Intent(CurrentEvents.this, HostEvents.class);
				} else {
					i = new Intent(CurrentEvents.this, UserInvites.class);
				}

				i.putExtras(b);
				startActivityForResult(i, 0);
			}

			adapter = new SeperatedListAdapter(CurrentEvents.this, facebookUser);
			setListAdapter(adapter);
			adapter.setData(CurrentEvents.this.service);
			
			CurrentEvents.this.service.addDataRequest(new DataRequestRefresh(false), new IRequestListener<PubEventArray>(){

				public void onRequestComplete(PubEventArray data) {
					runOnUiThread(new Runnable(){

						public void run() {
							refreshList();
						}});
				}

				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, e.getMessage());
				}});
		}

		public void onServiceDisconnected(ComponentName className) {

		}

	};

	class SeperatedListAdapter extends BaseAdapter {

		private static final String hostingSavedString = "Saved Invites";
		private static final String respondedToString = "Responded to";
		private static final String hostSentString = "Sent invites";
		private static final String waitingForResponseString = "Waiting for response";
		public final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();
		public final ArrayAdapter<String> headers;
		public final static int TYPE_SECTION_HEADER = 0;

		private User currentUser;

		public SeperatedListAdapter(Context context, AppUser facebookUser) {
			this.currentUser = facebookUser;

			headers = new ArrayAdapter<String>(context, R.layout.header);
		}

		// Read the data out of the service and put it in to the adapters
		public void setData(IPubService serviceInterface) {
			ArrayAdapter<PubEvent> hostingSaved = new ArrayAdapter<PubEvent>(
					CurrentEvents.this.getApplicationContext(),
					R.layout.list_item);
			for (PubEvent savedEvent : serviceInterface.GetSavedEvents()) {
				hostingSaved.add(savedEvent);
			}
			
			ArrayAdapter<PubEvent> hostingSent = new ArrayAdapter<PubEvent>(
					CurrentEvents.this.getApplicationContext(),
					R.layout.list_item);
			for (PubEvent sentEvent : serviceInterface.GetSentEvents()) {
				hostingSent.add(sentEvent);
			}

			ArrayAdapter<PubEvent> waitingForResponses = new ArrayAdapter<PubEvent>(
					CurrentEvents.this.getApplicationContext(),
					R.layout.list_item);
			ArrayAdapter<PubEvent> respondedto = new ArrayAdapter<PubEvent>(
					CurrentEvents.this.getApplicationContext(),
					R.layout.list_item);

			for (PubEvent event : serviceInterface.GetInvitedEvents()) {
				if (event.GetUserGoingStatus(currentUser) == GoingStatus.maybeGoing) {
					waitingForResponses.add(event);
				} else {
					respondedto.add(event);
				}
			}

			// Keep in this order unless you want it to break!!!
			addSection(waitingForResponseString, waitingForResponses);
			addSection(hostSentString, hostingSent);
			addSection(respondedToString, respondedto);
			addSection(hostingSavedString, hostingSaved);

		}

		public void addSection(String section, Adapter adapter) {
			this.headers.add(section);
			this.sections.put(section, adapter);
		}

		public int getCount() {
			// total together all sections, plus one for each section header
			int total = 0;
			for (Adapter adapter : this.sections.values())
				total += adapter.getCount() + 1;
			return total;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			int sectionnum = 0;
			int oldPosition = position;
			for (Object section : this.sections.keySet()) {
				Adapter adapter = sections.get(section);
				int size = adapter.getCount() + 1;

				// check if position inside this section
				if (position == 0)
					return headers.getView(sectionnum, convertView, parent);
				if (position < size) {
					View v = adapter.getView(position - 1, convertView, parent);
					PubEvent specificEvent = (PubEvent) getItem(oldPosition);
					if (specificEvent.getCurrentStatus() == EventStatus.itsOn) {
						v.setBackgroundColor(Color.GREEN);
					} else if (specificEvent.getCurrentStatus() == EventStatus.itsOff) {
						v.setBackgroundColor(Color.RED);
					} else {
						v.setBackgroundColor(Color.YELLOW);
					}
					return v;
				}

				// otherwise jump into next section
				position -= size;
				sectionnum++;
			}

			return null;
		}

		// Gets the PubEvent from the position, counting through the lists
		public Object getItem(int position) {
			// Identify section in
			for (Adapter sectionAdapter : sections.values()) {
				--position; // discount the header

				if (position < sectionAdapter.getCount()) // does this position
															// fit in to this
															// list?
				{
					return sectionAdapter.getItem(position);
				} else // no? ok move on to the next list discounting the length
						// of this list
				{
					position -= sectionAdapter.getCount();
				}
			}
			Log.d(Constants.MsgError, "Couldn't find position");
			return null;
		}

		// Gets the category the item is in
		public int getItemCategory(int position) {
			int i = 0;
			for (Adapter sectionAdapter : sections.values()) {
				--position; // discount the header

				if (position < sectionAdapter.getCount()) {
					return i;
				} else {
					position -= sectionAdapter.getCount();
					++i;
				}
			}
			Log.d(Constants.MsgError, "Couldn't find position");
			return -1;
		}
	}
}

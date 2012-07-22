package dimappers.android.pub;

import java.util.ArrayList;
import java.util.List;

import net.awl.appgarden.sdk.AppGardenAgent;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.EventListItem;
import dimappers.android.PubData.EventStatus;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.ListHeader;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.User;

public class CurrentEvents extends ListActivity {
	SeparatedListAdapter adapter;
	IPubService service = null;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_events);
		
		AppGardenAgent.passExam("CURRENTEVENTS ONCREATE CALLED");

		bindService(new Intent(this, PubService.class), connection, 0);
		
		registerForContextMenu(getListView());
	}

	
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

	
	public void onResume() {
		super.onResume();
		refreshList();
	}
	
	
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    int pos = ((AdapterContextMenuInfo)menuInfo).position;
	    
	    if(adapter.getItem(pos) instanceof PubEvent) //if we have not selected a category header
	    {
	    	PubEvent event = (PubEvent)adapter.getItem(pos);
		    MenuInflater inflater = getMenuInflater();
		    
		    int category = adapter.getItemCategory(pos);
		    
		    switch (category) {
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
		    menu.setHeaderTitle(event.toString());
	    }
	    
	}
	
	
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
		            service.DeleteEvent(selectedEvent);
		            refreshList();
		            return true;
		            
		        //Saved event - click send
		        case R.id.host_saved_menu_item_send:
		        	Log.d(Constants.MsgInfo, "Send event");
		        	service.SendEvent(selectedEvent, new IRequestListener<PubEvent>() {
						
						
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
		        	service.ConfirmEvent(selectedEvent, new IRequestListener<PubEvent>() {
						
						public void onRequestFail(Exception e)
						{
							// TODO Auto-generated method stub
							
						}
						
						public void onRequestComplete(PubEvent data)
						{
							runOnUiThread(new RefreshListRunnable());							
						}
					});
					refreshList();
		        	return true;
		        	
		        case R.id.host_sent_menu_item_cancel:
		        	service.CancelEvent(selectedEvent, new IRequestListener<PubEvent>() {
						
						public void onRequestFail(Exception e)
						{
							// TODO Auto-generated method stub
				        	
						}
						
						public void onRequestComplete(PubEvent data)
						{
							runOnUiThread(new RefreshListRunnable());
						}
					});
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

	class RefreshListRunnable implements Runnable
	{

		public void run()
		{
			refreshList();
		}
	}
	
	private void refreshList() {
		if(service!=null)
		{
			adapter.setData(service);
		}
	}

	
	public void onDestroy() {
		super.onDestroy();
		unbindService(connection);
		service = null;
	}
	
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
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

			adapter = new SeparatedListAdapter(CurrentEvents.this, new ArrayList<EventListItem>());
			adapter.setData(CurrentEvents.this.service);
			getListView().setAdapter(adapter);
			
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
	
	class SeparatedListAdapter extends ArrayAdapter<EventListItem>
	{
		List<EventListItem> items;
		int posOfStartOfSent = 0;
		int posOfStartOfInvited = 0;
		int posOfStartOfResponded = 0;
		int posOfStartOfSaved = 0;
		
		private static final String hostingSavedString = "Saved Invites";
		private static final String respondedToString = "Responded to";
		private static final String hostSentString = "Sent invites";
		private static final String waitingForResponseString = "Waiting for response";
		
		public SeparatedListAdapter(Context context, List<EventListItem> list) {
			super(context, R.layout.list_item, R.id.list_item_title, list);
			items = list;
		}
		
		public int getItemCategory(int pos) {
				 if(pos < posOfStartOfSent) 			{return -1;}
			else if(pos < posOfStartOfInvited - 1) 		{return Constants.HostedEventSent;}
			else if(pos < posOfStartOfResponded - 1) 	{return Constants.ProposedEventNoResponse;}
			else if(pos < posOfStartOfSaved - 1) 		{return Constants.ProposedEventHaveResponded;}
			else if(pos < items.size())					{return Constants.HostedEventSaved;}
			else										{return -1;}
		}

		public void setData(IPubService serviceInterface) {
			
			items.clear();
			
			//NOTE: when changing order of these, make sure the order of the positions of starts are altered (and change is reflected in getItemCategory(..)
			
			User currentUser = service.GetActiveUser();
			
			items.add(new ListHeader(hostSentString));
			posOfStartOfSent = items.size();
			for (PubEvent sentEvent : serviceInterface.GetSentEvents())
			{
				items.add(sentEvent);
			}

			List<PubEvent> waitingForResponses = new ArrayList<PubEvent>();
			List<PubEvent> respondedto = new ArrayList<PubEvent>();
			
			for (PubEvent event : serviceInterface.GetInvitedEvents()) {
				if (event.GetUserGoingStatus(currentUser) == GoingStatus.maybeGoing) {
					waitingForResponses.add(event);
				} else {
					respondedto.add(event);
				}
			}
			
			items.add(new ListHeader(waitingForResponseString));
			posOfStartOfInvited = items.size();
			for(PubEvent event : waitingForResponses)
			{
				items.add(event);
			}
			
			items.add(new ListHeader(respondedToString));

			posOfStartOfResponded = items.size();
			for(PubEvent event : respondedto)
			{
				items.add(event);
			}

			items.add(new ListHeader(hostingSavedString));
			posOfStartOfSaved = items.size();
			for (PubEvent savedEvent : serviceInterface.GetSavedEvents())
			{
				items.add(savedEvent);
			}
			
			notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(Constants.MsgInfo, "current postion:" + position);
			if(getItem(position) instanceof ListHeader)
			{
				View layout = (((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.header, null));
				TextView tv = (TextView)layout.findViewById(R.id.list_header_title);
				tv.setText(((ListHeader)getItem(position)).getHeader());
				return layout;
			}
			else if(getItem(position) instanceof PubEvent)
			{
				if(convertView==null || convertView.findViewById(R.id.list_item_title)==null)
				{
					convertView = (((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item, null));
				}

				PubEvent specificEvent = (PubEvent) getItem(position);
				
				try
				{
					super.getView(position, convertView, parent);
				}
				catch(NullPointerException e)
				{
					Log.d(Constants.MsgError, "This shouldn't really happen: " + e.getMessage());
					((TextView)convertView.findViewById(R.id.list_item_title)).setText(specificEvent.toString());
				}
				
				if (specificEvent.getCurrentStatus() == EventStatus.itsOn) {
					convertView.setBackgroundColor(Color.GREEN);
				} else if (specificEvent.getCurrentStatus() == EventStatus.itsOff) {
					convertView.setBackgroundColor(Color.RED);
				} else {
					convertView.setBackgroundColor(Color.YELLOW);
				}
				
				return convertView;
			}
			else
			{
				Log.d(Constants.MsgError, "This should never happen (CurrentEvents SeparatedListAdapter getView(..))");
				return null;
			}
		}
		
	}
}

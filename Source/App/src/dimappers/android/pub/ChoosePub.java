package dimappers.android.pub;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class ChoosePub extends LocationRequiringActivity implements OnClickListener {
		
		EditText pub_input;
		ListView pub_list;
		ArrayAdapter<Place> adapter;
		ArrayList<Place> listItems = new ArrayList<Place>();

		double latitude;
		double longitude;
	
		 @Override
		    public void onCreate(Bundle savedInstanceState) {
		    	super.onCreate(savedInstanceState);
		    	setContentView(R.layout.pub_choose);
		    	
		    	bindService(new Intent(this, PubService.class), connection, 0);
		  
		    	latitude = getIntent().getExtras().getDouble(Constants.CurrentLatitude);
		    	longitude = getIntent().getExtras().getDouble(Constants.CurrentLongitude);
		    	
		    	pub_input = (EditText)findViewById(R.id.input_pub);
		    	Typeface font = Typeface.createFromAsset(getAssets(), "SkratchedUpOne.ttf");
		    	((Button)findViewById(R.id.use_pub_button)).setTypeface(font);
		    	pub_list = (ListView)findViewById(android.R.id.list);
				adapter = new ArrayAdapter<Place>(this, android.R.layout.simple_list_item_1, listItems);
				setListAdapter(adapter);

		    	Button use_pub = (Button)findViewById(R.id.use_pub_button);
		    	use_pub.setOnClickListener(this);
		 }	 
		 
		 public void onClick(View v) {
			 switch(v.getId()) {
				 case R.id.use_pub_button : {
					 getPubs(pub_input.getText().toString().trim());
					break;
				 }
			 }
		 }
		 
		 public void onListItemClick(ListView l, View v, int pos, long id) {
			 super.onListItemClick(l,v,pos,id);
			 Place place = listItems.get(pos);
			 event.SetPubLocation(
					 new PubLocation(
							 (float)place.geometry.location.lat,
							 (float)place.geometry.location.lng,
							 place.name)
					 );
			 
			 /*Place_Detailed pd = null;
			try {
				pd = finder.performDetails(listItems.get(pos).reference).result;
			} catch (Exception e) {
				Log.d(Constants.MsgError,"Exception thrown from performDetails(...)");
				e.printStackTrace();
			}
			 Log.d(Constants.MsgInfo, pd.name + " has a rating of " + pd.rating);*/
			 
			 Intent i = new Intent();
			 i.putExtra(Constants.CurrentWorkingEvent, event.GetEventId());
			 this.setResult(RESULT_OK,i);
			 finish();
		 }
		 
		@Override
		public void onDestroy()
		{
			super.onDestroy();
			unbindService(connection);
		}
		 
	    void getPubs()
	    {
			getPubs("");
	    }
	    
	    void getPubs(String keyword)
	    {
	    	DataRequestPubFinder pubFinder;
	    	if(keyword.length()==0) {pubFinder = new DataRequestPubFinder(latitude, longitude);}
	    	else {pubFinder = new DataRequestPubFinder(latitude, longitude, keyword);}
	    	service.addDataRequest(pubFinder, new IRequestListener<PlacesList>() {

				public void onRequestComplete(PlacesList data) {
					if(data.status.equals("ZERO_RESULTS")) //this is when no places have been found
					{
						runOnUiThread(new Runnable(){

							public void run() {
								LocationChanger.changeLocation(ChoosePub.this);
							}});
					}
					else
					{
						runOnUiThread(new AdapterUpdater(data.results));
						Log.d(Constants.MsgInfo, "Pubs returned from DataRequest");
					}
				}

				public void onRequestFail(Exception e) {
					Log.d(Constants.MsgError, "Pubs not returned from DataRequest: " + e.getMessage());
					runOnUiThread(new Runnable(){
						
						public void run()
						{
							Toast.makeText(getApplicationContext(), "Pubs are currently unavaliable.", Toast.LENGTH_LONG).show();
							findViewById(R.id.progressBar).setVisibility(View.GONE);
						}
					});
				}});
	    }
		
	    void success(double lat, double lng, final String loc)
	    {
	    	latitude = lat;
	    	longitude = lng;
	    	getPubs(pub_input.getText().toString().trim());
	    }
	    
	    private class AdapterUpdater implements Runnable
	    {
	    	List<Place> places;
	    	
	    	AdapterUpdater(List<Place> places) {this.places = places;}

			public void run() {
				listItems.clear();
				for(Place p: places) {
					listItems.add(p);
				}
				adapter.notifyDataSetChanged();
				findViewById(R.id.progressBar).setVisibility(View.GONE);
			}
	    	
	    }
		
	    private ServiceConnection connection = new ServiceConnection()
	    {
	    	public void onServiceConnected(ComponentName className, IBinder pubService)
	    	{
	    		service = (IPubService)pubService;
	    		event = service.getEvent(getIntent().getExtras().getInt(Constants.CurrentWorkingEvent));
	    		getPubs();
    		}

    		public void onServiceDisconnected(ComponentName className)
    		{
    		}
    		
    	};
		 
}


package dimappers.android.pub;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class Pending extends Activity implements OnClickListener{
	TextView text;
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pending_guests);
    	text = (TextView) findViewById(R.id.location_error);
    	((TextView)findViewById(R.id.cancelbutton)).setOnClickListener(this);
    	findLocation();	
	}	
	
	//Finding current location
	private void findLocation()
	{		
		updateText("Finding current location");
		LocationFinder lc = new LocationFinder(this);
		Location location = lc.findLocation();
		giveLocation(location);
	 }
	public void updateText(String s) {
		text.setText(s);
	}	
	public void giveLocation(Location location)
	{
		DoLoading task = new DoLoading();
		task.SetLocation(location);
		task.execute(this);
	}
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.cancelbutton :{finish();}
		}
	}
}


class DoLoading extends AsyncTask<Pending,Integer,Integer>
{
	private PubEvent event;
	private Pending activity;
	private AppUser facebookUser;
	private Location location;
	@Override
	protected Integer doInBackground(Pending... params) {
        if(location == null)
        {
        	Log.d(Constants.MsgError, "Set location first!!");
        }
        else
        {
        	Log.d(Constants.MsgInfo, "Using location: " + location.getLatitude() + ", " + location.getLongitude());
        }
		
		activity = params[0];
        
        Bundle b = activity.getIntent().getExtras();
        if(b == null)
        {
        	Debug.waitForDebugger();        	
        }
        
        facebookUser = (AppUser)b.getSerializable(Constants.CurrentFacebookUser);
        
        publishProgress(Constants.CreatingEvent);
    	event = new PubEvent(Calendar.getInstance(), facebookUser);
    	
    	publishProgress(Constants.ChoosingPub);
    	findPub();
    	
    	publishProgress(Constants.PickingGuests);
    	//TODO: implement picking guests
    	event.AddUser(new AppUser(143));
    	event.AddUser(new AppUser(12341));
        
		return null;
	}
	protected void onProgressUpdate(Integer... progress) {
		if(progress[0].equals(Constants.CreatingEvent)) {activity.updateText("Creating new event");}
		else if(progress[0].equals(Constants.ChoosingPub)) {activity.updateText("Choosing a pub");}
		else if(progress[0].equals(Constants.PickingGuests)) {activity.updateText("Picking guests");}
	}	
	protected void onPostExecute(Integer result) {
		Bundle eventBundle = new Bundle();
		eventBundle.putAll(activity.getIntent().getExtras());
		eventBundle.putSerializable(Constants.CurrentWorkingEvent, event);
		eventBundle.putBoolean(Constants.IsSavedEventFlag, true);
		eventBundle.putDouble(Constants.CurrentLatitude, location.getLatitude());
		eventBundle.putDouble(Constants.CurrentLongitude, location.getLongitude());
		
		Intent intent = new Intent();
		intent.putExtras(eventBundle);
		
		activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }
	public void SetLocation(Location location)
	{
		this.location = location;
	}
	private void findPub() {
		PubFinder pubfinder = new PubFinder(location.getLatitude(),location.getLongitude());
		Place pub = new Place();
		pub.name="Unknown";
		double lat = 0;
		double lng = 0;
		try {
			List<Place> list = pubfinder.performSearch();
			if(list!=null&&list.size()!=0)
			{
				pub = list.get(0);
				lat = pub.geometry.location.lat;
				lng = pub.geometry.location.lng;
			}
		} catch (Exception e) {
			Log.d(Constants.MsgError,"Error while finding pubs.");
			e.printStackTrace();
		}
		event.SetPubLocation(new PubLocation((float)lat,(float)lng,pub.name));
	}
}

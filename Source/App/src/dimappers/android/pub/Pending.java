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
	AppUser facebookUser;
	PubEvent event; 
	Location currentLocation;
	boolean personFinished;
	boolean pubFinished;
	
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
		currentLocation = lc.findLocation();
		startTasks(currentLocation);
	 }
	public void updateText(String s) {
		text.setText(s);
	}	
	public void startTasks(Location location) {
		
        if(location == null){Log.d(Constants.MsgError, "Need to set location first.");}
        else{Log.d(Constants.MsgInfo, "Using location: " + location.getLatitude() + ", " + location.getLongitude());}
        
		createEvent();
        
		Object[] info = new Object[2];
		info[0] = location;
		info[1] = this;

		new PubFinding().execute(info);
		
		new PersonFinder().execute(this);	
	}
	public void createEvent() {
        updateText("Creating Event");
        
        Bundle b = getIntent().getExtras();
        if(b == null){Debug.waitForDebugger();}
        
        facebookUser = (AppUser)b.getSerializable(Constants.CurrentFacebookUser);
        event = new PubEvent(Calendar.getInstance(), facebookUser);
	}
	public void onClick(View v)
	{
		if(v.getId()==R.id.cancelbutton) {finish();}
	}
	public void onFinish() {
		Bundle eventBundle = new Bundle();
		eventBundle.putAll(getIntent().getExtras());
		eventBundle.putSerializable(Constants.CurrentWorkingEvent, event);
		eventBundle.putBoolean(Constants.IsSavedEventFlag, true);
		eventBundle.putDouble(Constants.CurrentLatitude, currentLocation.getLatitude());
		eventBundle.putDouble(Constants.CurrentLongitude, currentLocation.getLongitude());
		
		Intent intent = new Intent();
		intent.putExtras(eventBundle);
		
		setResult(Activity.RESULT_OK, intent);
        finish();
	}
}

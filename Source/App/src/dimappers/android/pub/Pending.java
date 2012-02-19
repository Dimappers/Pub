package dimappers.android.pub;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import dimappers.android.PubData.User;

public class Pending extends Activity implements OnClickListener{
	
	private TextView text;
	private AppUser facebookUser;
	private Location currentLocation;
	
	PubEvent event; 
	
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
		
        if(location == null){Log.d(Constants.MsgError, "Need to set location first."); updateText("An error has occurred, please try again.");}
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

        
        facebookUser = AppUser.AppUserFromUser((User)b.getSerializable(Constants.CurrentFacebookUser));
        event = new PubEvent(Calendar.getInstance(), new User(facebookUser.getUserId()));
	}
	public void onClick(View v)
	{
		if(v.getId()==R.id.cancelbutton) {finish();}
	}
	public void onFinish() {
		setResult(Activity.RESULT_OK, new Intent().putExtras(fillBundle()));
        finish();
	}
	private Bundle fillBundle() {
		Bundle eventBundle = new Bundle();
		eventBundle.putAll(getIntent().getExtras());
		eventBundle.putSerializable(Constants.CurrentWorkingEvent, event);
		eventBundle.putBoolean(Constants.IsSavedEventFlag, true);
		eventBundle.putDouble(Constants.CurrentLatitude, currentLocation.getLatitude());
		eventBundle.putDouble(Constants.CurrentLongitude, currentLocation.getLongitude());
		return eventBundle;

	}
	public void errorOccurred() {
	   	new AlertDialog.Builder(this).setMessage("An unexpected error has occurred. Please try again.")  
        .setTitle("Error")  
        .setCancelable(false)  
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {dialog.cancel(); finish();}}).show(); 
	}
}

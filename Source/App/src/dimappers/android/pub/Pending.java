package dimappers.android.pub;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.Toast;

public class Pending extends Activity {
	View v;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pending_guests);
    	findViewById(R.id.location_error).setVisibility(View.INVISIBLE);
    	//TODO: find suitable guests etc.
		new DoLoading().execute(this);
	}	
}

class DoLoading extends AsyncTask<Pending,Integer,Integer>
{
	private PubEvent event;
	private Pending activity;
	private AppUser facebookUser;
	@Override
	protected Integer doInBackground(Pending... params) {
        activity = params[0];
        
        Bundle b = activity.getIntent().getExtras();
        if(b == null)
        {
        	Debug.waitForDebugger();        	
        }
        facebookUser = (AppUser)b.getSerializable("facebookId");
    	//Toast.makeText(activity.getApplicationContext(), "Received id: " + new Integer(facebookId).toString(), Toast.LENGTH_LONG).show();
    	event = new PubEvent(Calendar.getInstance(), facebookUser);
    	event.SetPubLocation(new PubLocation());
    	event.AddUser(new AppUser(143));
    	event.AddUser(new AppUser(12341));
        
		return null;
	}
	protected void onPostExecute(Integer result) {
		//TODO: pass updated event back
		activity.setResult(activity.RESULT_OK,activity.getIntent());
		Bundle eventBundle = new Bundle();
		eventBundle.putSerializable(Constants.CurrentWorkingEvent, event);
		eventBundle.putBoolean(Constants.IsSavedEventFlag, true);
		activity.getIntent().putExtras(eventBundle);
        activity.finish();
    }
}

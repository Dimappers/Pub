package dimappers.android.pub;

import java.util.Calendar;
import java.util.Date;

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
	private int facebookId;
	@Override
	protected Integer doInBackground(Pending... params) {
        activity = params[0];
        
        Bundle b = activity.getIntent().getExtras();
        if(b == null)
        {
        	Debug.waitForDebugger();        	
        }
        facebookId = b.getInt("facebookId");
    	//Toast.makeText(activity.getApplicationContext(), "Received id: " + new Integer(facebookId).toString(), Toast.LENGTH_LONG).show();
    	Integer fb = new Integer(facebookId);
    	AppUser host = new AppUser(fb);
    	event = new PubEvent(Calendar.getInstance(), (User)host);
    	event.SetPubLocation(new PubLocation());
    	event.AddUser(new AppUser(143));
    	event.AddUser(new AppUser(12341));
        
		return null;
	}
	protected void onPostExecute(Integer result) {
		//TODO: pass updated event back
		activity.setResult(activity.RESULT_OK,activity.getIntent());
		
		Bundle eventBundle = new Bundle();
		eventBundle.putSerializable("event", event);
		eventBundle.putBoolean("NewEvent", true);
		activity.getIntent().putExtras(eventBundle);
        activity.finish();
    }
}

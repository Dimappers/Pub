package dimappers.android.pub;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

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
	private Pending activity;
	@Override
	protected Integer doInBackground(Pending... params) {
		//pretending to be doing something - needs removing once app is *actually* doing something
        long t0, t1;
        activity = params[0];
        t0 =  System.currentTimeMillis();
        do {
        	t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (1 * 1000));
		//end of fake wait
        
		return null;
	}
	protected void onPostExecute(Integer result) {
		//TODO: pass updated event back
		activity.setResult(activity.RESULT_OK,activity.getIntent());
        activity.finish();
    }
}

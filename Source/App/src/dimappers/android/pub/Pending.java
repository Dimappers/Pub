package dimappers.android.pub;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class Pending extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pending_guests);
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
        while ((t1 - t0) < (3 * 1000));
		//end of fake wait
		return null;
	}
	protected void onPostExecute(Integer result) {
        activity.finish();
    }
}

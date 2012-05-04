package dimappers.android.pub;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import android.app.ListActivity;
import android.util.Log;
import android.widget.Toast;

public abstract class LocationRequiringActivity extends ListActivity {
	
	public IPubService service;
	public PubEvent event;
	
	void failure(int which)
	{
		Log.d(Constants.MsgError, "Error using custom location!!");
		if(which==0) //no pubs found
		{
			runOnUiThread(new Runnable(){
				public void run() {
					Toast.makeText(getApplicationContext(), "No pubs found near this location", Toast.LENGTH_SHORT).show();
				}});
		}
		else if(which==1) //error finding pubs
		{
			runOnUiThread(new Runnable(){
			public void run() {
				Toast.makeText(getApplicationContext(), "Pubs unable to be found", Toast.LENGTH_SHORT).show();
			}});
		}
		else if(which==2) //error when geocoding
		{
			runOnUiThread(new Runnable(){
				public void run() {
					Toast.makeText(getApplicationContext(), "Unrecognised location", Toast.LENGTH_SHORT).show();
				}});
		}
		else //this shouldn't happen
		{
			runOnUiThread(new Runnable(){
				public void run() {
					Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
				}});
		}
	}
	
	abstract void success(double lat, double lng, final String loc);

}

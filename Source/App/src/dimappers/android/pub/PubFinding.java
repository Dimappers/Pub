package dimappers.android.pub;

import java.util.List;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

<<<<<<< HEAD

=======
>>>>>>> 285ffae13f75020fbf5fac692aed6426171dbf8b
public class PubFinding extends AsyncTask<Object, Integer, Boolean> {
	Location location;
	Pending activity;
	
	@Override
<<<<<<< HEAD

=======
>>>>>>> 285ffae13f75020fbf5fac692aed6426171dbf8b
	protected Boolean doInBackground(Object... params) { 
		try {
			location = (Location) params[0];
			activity = (Pending) params[1];
		}
<<<<<<< HEAD

=======
>>>>>>> 285ffae13f75020fbf5fac692aed6426171dbf8b
		catch(Exception e) {Log.d(Constants.MsgError,"Wrong input entered."); return false;}
		
    	publishProgress(Constants.ChoosingPub);
		return findPub();
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		if(progress[0].equals(Constants.ChoosingPub)) {activity.updateText("Choosing a pub");}
	}
	
	@Override
<<<<<<< HEAD

=======
>>>>>>> 285ffae13f75020fbf5fac692aed6426171dbf8b
	protected void onPostExecute(Boolean result)
	{
		if(!result) {activity.errorOccurred();}
		else {
			if(activity.personFinished) {activity.onFinish();}
			else {activity.pubFinished=true;}
		}
	}
	
	private Boolean findPub() {
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
<<<<<<< HEAD

=======
>>>>>>> 285ffae13f75020fbf5fac692aed6426171dbf8b
			return false;
		}
		activity.event.SetPubLocation(new PubLocation((float)lat,(float)lng,pub.name));
		return true;
	}
}

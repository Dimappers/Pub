package dimappers.android.pub;

import java.util.List;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class PubFinding extends AsyncTask<Object, Integer, Boolean> {
	Location location;
	Pending activity;
	List<Place> places;
	
	@Override
	protected Boolean doInBackground(Object... params) { 
		try {
			location = (Location) params[0];
			activity = (Pending) params[1];
		}
		catch(Exception e) {Log.d(Constants.MsgError,"Wrong input entered."); return false;}
		
		return findPub();
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		if(progress[0].equals(Constants.ChoosingPub)) {activity.updateText("Choosing a pub");}
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		if(!result) {activity.errorOccurred();}
		else {
			activity.setLocations(places);
			if(activity.personFinished) {activity.onFinish();}
			else {activity.pubFinished=true;}
		}
	}
	
	public void updateProgress(Integer progress) {
    	publishProgress(progress);
	}
	
	private Boolean findPub() {
		try {
			places = new PubFinder(location.getLatitude(),location.getLongitude()).performSearch();
			updateProgress(Constants.ChoosingPub);
			if(places==null) {return false;}
			return true;
		} catch (Exception e) {
			Log.d(Constants.MsgError,"Error while finding pubs.");
			e.printStackTrace();
			return false;
		}
	}
}

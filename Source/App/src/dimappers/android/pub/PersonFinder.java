package dimappers.android.pub;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;
import android.os.AsyncTask;

public class PersonFinder extends AsyncTask<Pending, Integer, Integer> {
	Pending activity;
	
	@Override
	protected Integer doInBackground(Pending... params) {
		
		activity = (Pending) params[0];
		
		doFacebookCall();
		publishProgress(Constants.PickingGuests);
		
    	//TODO: implement picking guests
    	activity.event.AddUser(new User(143L));
    	activity.event.AddUser(new User(12341L));
    	activity.event.AddUser(new User(237016L));
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		if(progress[0].equals(Constants.PickingGuests)) {activity.updateText("Picking guests");}
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if(activity.pubFinished) {activity.onFinish();}
		else {activity.personFinished=true;}
	}

	private void doFacebookCall() {
		//TODO: Write method
	}
}

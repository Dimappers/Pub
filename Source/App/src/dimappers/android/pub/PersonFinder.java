package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;
import android.os.AsyncTask;
import android.util.Log;

public class PersonFinder {
	Pending activity;
	IPubService service;
	
	PersonFinder(Pending activity, IPubService service)
	{
		this.activity = activity;
		this.service = service;
	}
	
	public void getFriends()
	{	
		if(!Constants.emulator) {
			doFacebookCall();
		}
		else 
		{
			activity.allFriends = new AppUser[1];
			activity.allFriends[0] = new AppUser(555L, "Test AppUser");
		}

		activity.updateText("Picking guests");
	}

	private void doFacebookCall() {
		
		DataRequestGetFriends friends = new DataRequestGetFriends();
		service.addDataRequest(friends, new IRequestListener<AppUserArray>(){

			public void onRequestComplete(AppUserArray data) {
				activity.allFriends = data.getArray();
				if(activity.pubFinished) {activity.onFinish();}
				else {activity.personFinished=true;}
			}

			public void onRequestFail(Exception e) {
				activity.errorOccurred();
			}});
		
	}
}

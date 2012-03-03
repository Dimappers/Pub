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

public class PersonFinder extends AsyncTask<Object, Integer, Boolean> {
	Pending activity;
	IPubService service;
	Facebook facebook;
	
	@Override
	protected Boolean doInBackground(Object... params) {
		
		try {
			activity = (Pending) params[0];
			service = (IPubService) params[1];
		}
		catch(Exception e) {Log.d(Constants.MsgError, "Wrong input entered."); return false;}
		
		if(!Constants.emulator) {
			facebook = service.GetFacebook();
			doFacebookCall();
		}
		else {
			activity.event.AddUser(new AppUser(555L, "Test AppUser"));
		}
		publishProgress(Constants.PickingGuests);
		
		return true;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		if(progress[0].equals(Constants.PickingGuests)) {activity.updateText("Picking guests");}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if(!result) {activity.errorOccurred();}
		else {
			if(activity.pubFinished) {activity.onFinish();}
			else {activity.personFinished=true;}
		}
	}

	private void doFacebookCall() {
		JSONObject friends = null;
		try {
			friends = new JSONObject(facebook.request("me/friends"));
			JSONArray jasonsFriends = friends.getJSONArray("data");
			for (int i=0; i < jasonsFriends.length(); i++)
			{
				JSONObject jason = (JSONObject) jasonsFriends.get(i);
				activity.facebookFriends.add(new AppUser(Long.parseLong(jason.getString("id")), jason.getString("name")));
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

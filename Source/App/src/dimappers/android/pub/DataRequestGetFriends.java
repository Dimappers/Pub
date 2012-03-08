package dimappers.android.pub;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;

public class DataRequestGetFriends implements IDataRequest<Long, AppUserArray> {

	IPubService service;
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<AppUserArray> listener,
			HashMap<Long, AppUserArray> storedData) {
		Facebook facebook = service.GetFacebook();
		if(storedData.size() > 0) //if we have retrieved the friends before don't bother getting again unless they are more than a week old
		{
			if(!storedData.get(0L).isOutOfDate())
			{
				listener.onRequestComplete(storedData.get(0L)); //Friends last got one week ago so we are done - TODO: Test me!!
				return;
			}
		}
		Log.d(Constants.MsgInfo, "Getting friends");
		JSONObject mefriends = null;
		try {
			mefriends = new JSONObject(facebook.request("me/friends"));
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		} 
		try {
			JSONArray jasonsFriends = mefriends.getJSONArray("data");
			AppUser[] friends = new AppUser[jasonsFriends.length()];
			for (int i=0; i < jasonsFriends.length(); i++)
			{
				JSONObject jason = (JSONObject) jasonsFriends.get(i);
				Long id = Long.parseLong(jason.getString("id"));
				friends[i] = new AppUser(id, jason.getString("name"));
			}
			AppUserArray friendsArray = new AppUserArray(friends);
			storedData.put(0L, friendsArray);
			listener.onRequestComplete(friendsArray);
			return;
		} catch (JSONException e) {
			listener.onRequestFail(e);
			return;
		}
	}

	@Override
	public String getStoredDataId() {
		return "AppUsers";
	}

}

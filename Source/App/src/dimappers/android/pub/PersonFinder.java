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
	IPubService service;
	
	PersonFinder(IPubService service)
	{
		this.service = service;
	}
	
	public void getFriends(final IRequestListener<AppUserArray> listener)
	{	
		if(!Constants.emulator) {
			DataRequestGetFriends friends = new DataRequestGetFriends();
			service.addDataRequest(friends, listener);
		}
		else 
		{
			AppUser[] users = new AppUser[] { new AppUser(555L, "Test AppUser") };
			listener.onRequestComplete(new AppUserArray(users));
		}
	}
}

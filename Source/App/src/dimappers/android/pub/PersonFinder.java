package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PersonFinder {
	IPubService service;
	Context context;
	
	PersonFinder(IPubService service, Context context)
	{
		this.service = service;
		this.context = context;
	}
	
	public void getFriends(final IRequestListener<AppUserArray> listener)
	{	
		DataRequestGetFriends friends = new DataRequestGetFriends(context);
		service.addDataRequest(friends, listener);
	}
}

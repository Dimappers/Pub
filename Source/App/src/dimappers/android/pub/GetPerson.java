package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class GetPerson extends AsyncTask<Object, Void, AppUser> {

	IRequestListener<AppUser> listener;
	
	@Override
	protected AppUser doInBackground(Object... bundle) {
		listener = (IRequestListener<AppUser>) bundle[0];
		Facebook facebook = (Facebook) bundle[1];
		JSONObject me;
		try {
			Log.d(Constants.MsgInfo, "Getting information about current user");
			me = new JSONObject(facebook.request("me"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "Malformed url when requesting info about current facebook user: " + e.getMessage());
			return null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "Jason: " + e.getMessage());
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "IO when retrieving current user: " + e.getMessage());
			return null;
		}
    	String id = null;
    	String name = null;
		try {
			id = me.getString("id");
			name = me.getString("name");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "Jason error in reading info about current user: " + e.getMessage());
			return null;
		}
    	
		Log.d(Constants.MsgInfo, "Logged in as user: " + name + " with ID: " + id);
		
    	try
    	{
    		return new AppUser(Long.parseLong(id), name);
    	}
    	catch(NumberFormatException e)
    	{
    		Log.d(Constants.MsgError, e.getMessage());
    		return null;
    	}
	}
	
	@Override
	protected void onPostExecute(AppUser appUser)
	{
		if(appUser!=null) {listener.onRequestComplete(appUser);}
		else {listener.onRequestFail(new Exception());}
	}

}

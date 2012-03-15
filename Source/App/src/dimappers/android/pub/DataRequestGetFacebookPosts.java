package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;

public class DataRequestGetFacebookPosts implements IDataRequest<String, XmlJasonObject>
{
	IPubService service;
	
	public DataRequestGetFacebookPosts() {}
	
	public void giveConnection(IPubService connectionInterface)
	{
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<XmlJasonObject> listener, HashMap<String, XmlJasonObject> storedData)
	{
		Facebook facebook = service.GetFacebook();
		XmlJasonObject xmlPosts = null;
		
		//TODO: Check if out of date
		if(storedData.containsKey("posts"))
		{
			Log.d(Constants.MsgInfo, "Already have posts.");
			listener.onRequestComplete(storedData.get("posts"));
			return;
		}
		
		Log.d(Constants.MsgInfo, "Getting posts from Facebook.");
		try
		{
			xmlPosts =  new XmlJasonObject(facebook.request("me/feed"));
		} 
		catch (Exception e)
		{
			listener.onRequestFail(e);
			return;
		}
		
		storedData.put("posts", xmlPosts);
		listener.onRequestComplete(xmlPosts);		
	}

	public String getStoredDataId() {
		return "JSONObject";
	}	
}
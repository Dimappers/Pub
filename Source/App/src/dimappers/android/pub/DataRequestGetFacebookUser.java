package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;

public class DataRequestGetFacebookUser implements
		IDataRequest<Long, AppUser>
{
	long facebookIdToGet;
	boolean isMe;
	IPubService service;
	public DataRequestGetFacebookUser(long facebookId)
	{
		facebookIdToGet = facebookId;
		isMe = false;
	}
	
	//Don't pass an id in to do it about the current user
	public DataRequestGetFacebookUser()
	{
		isMe = true;
		facebookIdToGet = -1;
	}
	
	public void giveConnection(IPubService connectionInterface)
	{
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<AppUser> listener,
			HashMap<Long, AppUser> storedData)
	{
		Facebook facebook = service.GetFacebook();
		AppUser appUser = null;
		if(!isMe) //not me
		{
			if(storedData.containsKey(facebookIdToGet)) //am I stored
			{
				appUser = storedData.get(facebookIdToGet);
			}
			else
			{
				//Not stored
				try
				{
					appUser = AppUser.AppUserFromUser(new User(facebookIdToGet), facebook);
				} catch (Exception e)
				{
					listener.onRequestFail(e);
					return;
				}
			}
		}
		else //is me
		{
			if(service.GetActiveUser() != null) //not in the service
			{
				JSONObject me;
				try
				{
					me = new JSONObject(facebook.request("me"));
					String id = me.getString("id");
					String name = me.getString("name");
					appUser = new AppUser(Long.parseLong(id), name);
				}
				catch(Exception e)
				{
					listener.onRequestFail(e);
					return;
				}
			}
			else //Are in the service
			{
				appUser = service.GetActiveUser();
			}
		}
		storedData.put(facebookIdToGet, appUser);
		listener.onRequestComplete(appUser);		
	}

	public String getStoredDataId() {
		return "AppUser";
	}	
}
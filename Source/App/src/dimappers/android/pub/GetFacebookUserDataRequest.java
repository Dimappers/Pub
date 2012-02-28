package dimappers.android.pub;

import java.util.ArrayList;
import java.util.HashMap;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;

public class GetFacebookUserDataRequest implements
		IDataRequest<Long, AppUser>
{
	long facebookIdToGet;
	IPubService service;
	public GetFacebookUserDataRequest(long facebookId)
	{
		facebookIdToGet = facebookId;
	}
	
	public void giveConnection(IPubService connectionInterface)
	{
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<AppUser> listener,
			HashMap<Long, AppUser> storedData)
	{
		if(storedData.containsKey(facebookIdToGet))
		{
			listener.onRequestComplete(storedData.get(facebookIdToGet));
		}
		
		Facebook facebook = service.GetFacebook();
		
		AppUser appUser = AppUser.AppUserFromUser(new User(facebookIdToGet), facebook);
		listener.onRequestComplete(appUser);
		storedData.put(facebookIdToGet, appUser);		
	}

	public String getStoredDataId() {
		return "AppUser";
	}	
}
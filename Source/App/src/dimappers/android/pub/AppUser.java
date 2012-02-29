package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;

import android.provider.MediaStore.Images;
import android.util.Log;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;

public class AppUser extends User
{
	//Properties
	private String facebookName;

	//Constructors
	public AppUser(Long facebookUserId)
	{
		super(facebookUserId);
	}
	
	public AppUser(Long facebookUserId, String name)
	{
		super(facebookUserId);
		facebookName = name;
	}
	
	public String toString()
	{
		return facebookName;
	}
	
	//Public methods
	
	//Get facebook profile picture
	/*Images GetFacebookImage()
	{
		return null;		
	}
	
	//Get full facebook name, ie Thomas Kiley instead of thomas.kiley
	String GetRealFacebookName()
	{
		return getUserId().toString();
	}
	
	@Override
	public String toString()
	{
		return "Id: " + GetRealFacebookName();		
	}
	*/
	public static AppUser AppUserFromUser(User user, Facebook facebook) throws MalformedURLException, JSONException, IOException
	{
		JSONObject them;
		them = new JSONObject(facebook.request(Long.toString(user.getUserId())));
		return new AppUser(user.getUserId(), them.getString("name"));
	}
}

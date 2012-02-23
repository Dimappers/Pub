package dimappers.android.pub;

import com.facebook.android.Facebook;

import android.provider.MediaStore.Images;
import dimappers.android.PubData.User;

public class AppUser extends User
{
	//Properties


	//Constructors
	private AppUser(Long facebookUserId)
	{
		super(facebookUserId);
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
	
	public static AppUser AppUserFromUser(User user)
	{
		return new AppUser(user.getUserId());
	}*/
	
	public static String getFacebookName(User user)
	{
		return "Name: " + user.getUserId();
	}
	

}

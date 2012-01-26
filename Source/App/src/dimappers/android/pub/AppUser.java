package dimappers.android.pub;

import android.provider.MediaStore.Images;
import dimappers.android.PubData.User;
import dimappers.android.PubData.PubTripState;

public class AppUser extends User
{
	//Properties
	
	//Constructors
	public AppUser(Integer facebookUserId)
	{
		super(facebookUserId);
	}
	
	//Public methods
	
	//Get facebook profile picture
	Images GetFacebookImage()
	{
		return null;		
	}
	
	//Get full facebook name, ie Thomas Kiley instead of thomas.kiley
	String GetRealFacebookName()
	{
		return "";
	}

}

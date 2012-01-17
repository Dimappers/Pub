package dimappers.android.PubData;

import java.io.Serializable;
import java.util.HashMap;

/* This class holds information about a guest
 * It does nothing with this data, it is purely a data store
 * 
 * This class should be overridden in both the App and the Server
 * 	In the app can contain extra GUI/Facebook stuff like profile pic, real name
 * 	In the server it should contain details of how to message the person (eg do they have the app)
 * 	In the future this may contain authentication data and is the class that should be sent to 
 *  identify who is using the app
 * Author: TK
 */
public class Guest implements Serializable
{
	//Properties
	private String 			facebookUserName;
	
	//Constructors
	public Guest(String facebookUserName)
	{
		this.facebookUserName = facebookUserName;
	}
	
	//Getter/Setter methods
	public String GetFacebookUserName()
	{
		return facebookUserName;
	}
}

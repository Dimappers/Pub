package dimappers.android.PubData;

import java.io.Serializable;

/*
 * This class holds data about the User. It should be overridden in the app and the 
 * server to extend functionality. 
 * The server will contain the id's of the events the user is a part of.
 *
 * Author: MF
 */
public class User implements Serializable {
	
	//Properties
	private Integer facebookUserId;
	
	//Constructor
	public User(Integer facebookUserId) {
		this.facebookUserId = facebookUserId;
	}
	
	//Encapsulation
	public Integer getUserId() 	{ return facebookUserId; }	
	
	public boolean isEqual(User otherUser)
	{
		return facebookUserId == otherUser.getUserId();
	}

}

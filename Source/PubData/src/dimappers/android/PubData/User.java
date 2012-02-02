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
	
	@Override
	public boolean equals(Object otherUser)
	{
		if(otherUser instanceof User)
		{
			return facebookUserId.equals(((User)otherUser).getUserId());
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return facebookUserId.hashCode();
	}

}

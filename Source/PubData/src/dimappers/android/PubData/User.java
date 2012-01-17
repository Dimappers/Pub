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
	private String facebookUserName;
	
	//Constructor
	public User(String facebookUserName) {
		this.facebookUserName = facebookUserName;
	}
	
	//Encapsulation
	public String getName() { return this.facebookUserName; }	

}

package dimappers.android.PubData;

import java.io.Serializable;

public class RefreshData implements Serializable{

	private User 	user;
	private boolean fullUpdate;
	
	//Constructor
	public RefreshData(User user, boolean fullUpdate) {
		this.user 	= user;		// The users unique id (facebook name) + authentication data
		this.fullUpdate = fullUpdate;	// If True, needs a full update
	}
	
	//Encapsulation
	public User 	getUser() 		{ return user; }
	public String 	getUserId()		{ return user.getName(); }
	public boolean 	isFullUpdate() 	{ return fullUpdate; }
}

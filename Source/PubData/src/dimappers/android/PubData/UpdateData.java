package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class UpdateData implements Serializable{

	private Date 						startTime;
	private PubLocation 				pubLocation;
	private LinkedList<User>			users;
	private int 						eventId;
	
	public UpdateData(int eventId, Date startTime, PubLocation pubLocation) {
		this.startTime = 	startTime;
		this.pubLocation = 	pubLocation;
		this.users = 		new LinkedList<User>();
	}
	
	public void addUser(User user) {
		users.add(user);
	}
	
	public int getEventId() 						{ return this.eventId; }
	public Date getStartTime() 						{ return this.startTime; }
	public PubLocation getPubLocation() 			{ return this.pubLocation; }
	public LinkedList<User> getUsers()	 			{ return this.users; }
}

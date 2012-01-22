package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class UpdateData implements Serializable{

	private Date 						startTime;
	private PubLocation 				pubLocation;
	private LinkedList<User>			guests;
	private int 						eventId;
	
	public UpdateData(int eventId, Date startTime, PubLocation pubLocation) {
		this.startTime = 	startTime;
		this.pubLocation = 	pubLocation;
		this.guests = 		new LinkedList<User>();
	}
	
	public void addGuest(User guest) {
		guests.add(guest);
	}
	
	public int getEventId() 						{ return this.eventId; }
	public Date getStartTime() 						{ return this.startTime; }
	public PubLocation getPubLocation() 			{ return this.pubLocation; }
	public LinkedList<User> getGuests() 			{ return this.guests; }
}

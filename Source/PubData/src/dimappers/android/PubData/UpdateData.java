package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class UpdateData implements Serializable{

	private Date 						startTime;
	private PubLocation 				pubLocation;
	private HashMap<User, GoingStatus>	guests;
	private int 						eventId;
	
	UpdateData(int eventId, Date startTime, PubLocation pubLocation) {
		this.startTime = 	startTime;
		this.pubLocation = 	pubLocation;
		this.guests = 		new HashMap<User, GoingStatus>();
	}
	
	public void addGuest(User guest) {
		guests.put(guest, GoingStatus.maybeGoing);
	}
	
	public void addGuest(User guest, GoingStatus goingStatus)
	{
		guests.put(guest, goingStatus);
	}
	
	public void SetGuestStatus(User guest, GoingStatus goingStatus)
	{
		guests.remove(guest);
		guests.put(guest, goingStatus);
	}
	
	public int getEventId() 						{ return this.eventId; }
	public Date getStartTime() 						{ return this.startTime; }
	public PubLocation getPubLocation() 			{ return this.pubLocation; }
	public HashMap<User, GoingStatus> getGuests() 	{ return this.guests; }
}

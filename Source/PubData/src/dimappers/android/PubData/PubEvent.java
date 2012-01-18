package dimappers.android.PubData;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;


/*This class holds information about a pub trip
 * Crucially, it takes no action with this data
 * For example, adding a guest will not invite that person
 * This is purely a data store
 * 
 * Author: TK
 */
public class PubEvent implements Serializable
{
	//Properties
	private LinkedList<User> 			guests;
	private Date 						startTime;
	private PubLocation					pubLocation;
	
	//Constructors
	public PubEvent(Date startTime, User host)
	{
		guests = new LinkedList<User>();
		this.startTime = startTime;
	}
	
	public PubEvent(Date startTime, PubLocation pubLocation, User host)
	{
		guests = new LinkedList<User>();
		guests.add(host);
		
		this.pubLocation = pubLocation;
		this.startTime = startTime;
	}
	
	//Getter/setter methods
	public LinkedList<User> GetGuests()
	{
		return guests;
	}
	
	public Date GetStartTime()
	{
		return startTime;
	}
	public void SetStartTime(Date startTime)
	{
		this.startTime = startTime;
	}
	
	public PubLocation GetPubLocation()
	{
		return pubLocation;
	}
	public void SetPubLocation(PubLocation pubLocation)
	{
		this.pubLocation = pubLocation;
	}
	
	//Public methods
	
	//Add a guest to the guest list
	public void AddGuest(User guest)
	{
		guests.add(guest);
	}
	
	//Remove a guest from the guest list
	public void RemoveGuest(User guest)
	{
		if(!guests.remove(guest))
		{
			System.out.println("Warning - tried to remove guest that wasn't there");
		}
	}
	
	//Remove a guest from the guest list by facebook user name
	public void RemoveGuest(String facebookUserName)
	{
		User guestToRemove = null;
		for(User guest : guests)
		{
			if(guest.getName() == facebookUserName)
			{
				guestToRemove = guest;
				break;
			}
		}
		
		if(guestToRemove != null)
		{
			guests.remove(guestToRemove);
		}
		else
		{
			System.out.println("Warning - tried to remove guest that wasn't there");
		}
	}
	
	public User GetHost() throws Exception
	{
		if(guests.size() < 1)
		{
			throw new Exception("No host for this event...");
		}
		
		return guests.getFirst();
	}
}

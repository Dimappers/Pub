package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


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
	private HashMap<User, GoingStatus>	guests;
	private User 						host;
	private Date 						startTime;
	private PubLocation					pubLocation;
	private int 						globalEventId;
	
	//Constructors
	public PubEvent(Date startTime, User host)
	{
		guests = new HashMap<User, GoingStatus>();
		guests.put(host, GoingStatus.going);
		this.host = host;
		this.startTime = startTime;
	}
	
	public PubEvent(Date startTime, PubLocation pubLocation, User host)
	{
		guests = new HashMap<User, GoingStatus>();
		guests.put(host, GoingStatus.going);
		this.host = host;
		this.pubLocation = pubLocation;
		this.startTime = startTime;
	}
	
	//Getter/setter methods
	public Set<User> GetGuests()
	{
		return guests.keySet();
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
	
	public int GetEventId()
	{
		return globalEventId;
	}
	public void SetEventId(int id)
	{
		globalEventId = id;		
	}
	
	//Public methods
	
	//Add a guest to the guest list
	public void AddGuest(User guest)
	{
		guests.put(guest, GoingStatus.maybeGoing);
	}
	public void AddGuest(User guest, GoingStatus status)
	{
		guests.put(guest, status);
	}
	
	//Remove a guest from the guest list
	public void RemoveGuest(User guest)
	{
		if(guests.containsKey(guest))
		{
			guests.remove(guest);
		}
		else
		{
			System.out.println("Warning - guest not there...");
		}
	}
	
	//Remove a guest from the guest list by facebook user name
	public void RemoveGuest(String facebookUserName)
	{
		User guestToRemove = null;
		for(User guest : guests.keySet())
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
	
	public User GetHost()
	{		
		return host;
	}
	
	public void UpdateGuestStatus(User user, boolean isGoing)
	{
		if(guests.containsKey(user))
		{
			guests.remove(user);
			GoingStatus status;
			if(isGoing)
			{
				status = GoingStatus.going;
			}
			else
			{
				status = GoingStatus.notGoing;
			}
			guests.put(user, status);
		}
	}
}

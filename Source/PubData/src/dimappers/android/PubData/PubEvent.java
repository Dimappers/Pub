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
	private HashMap<User, GoingStatus>	users;
	private User 						host;
	private Date 						startTime;
	private PubLocation					pubLocation;
	private int 						globalEventId;
	
	//Constructors
	public PubEvent(Date startTime, User host)
	{
		users = new HashMap<User, GoingStatus>();
		users.put(host, GoingStatus.going);
		this.host = host;
		this.startTime = startTime;
	}
	
	public PubEvent(Date startTime, PubLocation pubLocation, User host)
	{
		users = new HashMap<User, GoingStatus>();
		users.put(host, GoingStatus.going);
		this.host = host;
		this.pubLocation = pubLocation;
		this.startTime = startTime;
	}
	
	//Getter/setter methods
	public Set<User> GetUsers()
	{
		return users.keySet();
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
	public void AddUser(User user)
	{
		users.put(user, GoingStatus.maybeGoing);
	}
	public void AddUser(User user, GoingStatus status)
	{
		users.put(user, status);
	}
	
	//Remove a guest from the guest list
	public void RemoveUser(User user)
	{
		if(users.containsKey(user))
		{
			users.remove(user);
		}
		else
		{
			System.out.println("Warning - guest not there...");
		}
	}
	
	//Remove a guest from the guest list by facebook user name
	public void RemoveUser(String facebookUserName)
	{
		User guestToRemove = null;
		for(User guest : users.keySet())
		{
			if(guest.getName() == facebookUserName)
			{
				guestToRemove = guest;
				break;
			}
		}
		
		if(guestToRemove != null)
		{
			users.remove(guestToRemove);
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
	
	public void UpdateUserStatus(User user, boolean isGoing)
	{
		if(users.containsKey(user))
		{
			users.remove(user);
			GoingStatus status;
			if(isGoing)
			{
				status = GoingStatus.going;
			}
			else
			{
				status = GoingStatus.notGoing;
			}
			users.put(user, status);
		}
	}
}

package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Calendar;
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
	private HashMap<User, UserStatus>	users;
	private User 						host;
	private Calendar					startTime;
	private PubLocation					pubLocation;
	private int 						globalEventId;
	
	//Constructors
	public PubEvent(Calendar startTime, User host)
	{
		users = new HashMap<User, UserStatus>();
		users.put(host, new UserStatus(GoingStatus.going, startTime, null));
		this.host = host;
		this.startTime = startTime;
		globalEventId = Constants.EventIdNotAssigned;
	}
	
	public PubEvent(Calendar startTime, PubLocation pubLocation, User host)
	{
		users = new HashMap<User, UserStatus>();
		users.put(host, new UserStatus(GoingStatus.going, startTime, null));
		this.host = host;
		this.pubLocation = pubLocation;
		this.startTime = startTime;
		globalEventId = Constants.EventIdNotAssigned;
	}
	
	//Getter/setter methods
	public Set<User> GetUsers()
	{
		return users.keySet();
	}
	
	public HashMap<User, UserStatus> GetGoingStatusMap()
	{
		return users;
	}
	
	public GoingStatus GetUserGoingStatus(User user)
	{
		return users.get(user).goingStatus;
	}
	
	public Calendar GetStartTime()
	{
		return startTime;
	}
	public void SetStartTime(Calendar startTime)
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
	
	public boolean DoesContainUser(User user)
	{
		return users.containsKey(user);
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
		users.put(user, new UserStatus(GoingStatus.maybeGoing, startTime, null));
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

	public User GetHost()
	{		
		return host;
	}
	
	public void UpdateUserStatus(ResponseData response)
	{
		if(users.containsKey(response.GetUser()))
		{
			users.put(response.GetUser(), response.MakeUserStatus());
		}
	}
	
	public boolean isEqual(PubEvent event)
	{
		return event.GetEventId() == globalEventId;
	}
	
	@Override
	public String toString()
	{
		return pubLocation.pubName + " : " + GetFormattedStartTime();
	}
	
	public String GetFormattedStartTime()
	{
		String time;
		
		String date;
		Calendar currentTime = Calendar.getInstance();
		if(startTime.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH))
		{
			date = "Today";
		}
		else 
		{
			currentTime.add(Calendar.DAY_OF_MONTH, 1);
			if(currentTime.get(Calendar.DAY_OF_MONTH) == startTime.get(Calendar.DAY_OF_MONTH))
			{
				date = "Tomorrow";
			}
			else if(startTime.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
			{
				//Same year - don't bother with year
				date = startTime.get(Calendar.DAY_OF_MONTH) + "/" + (startTime.get(Calendar.MONTH) + 1);
			}
			else
			{
				date = startTime.get(Calendar.DAY_OF_MONTH) + "/" + (startTime.get(Calendar.MONTH) + 1) + startTime.get(Calendar.YEAR);
			}
		}
		
		time = date + " at " + startTime.get(Calendar.HOUR_OF_DAY) + ":" + startTime.get(Calendar.MINUTE);
		
		return time;
	}
}

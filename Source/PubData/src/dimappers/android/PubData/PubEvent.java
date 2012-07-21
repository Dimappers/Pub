package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom.Element;


/*This class holds information about a pub trip
 * Crucially, it takes no action with this data
 * For example, adding a guest will not invite that person
 * This is purely a data store
 * 
 * Author: TK
 */
public class PubEvent extends EventListItem implements Serializable, IXmlable
{
	private final String eventIdTag = "EventId";
	private final String usersTag = "Users";
	private final String invitedUserTag = "InvitedUser";
	private final String hostTag = "Host";
	private final String startTimeTag = "StartTime";
	private final String statusTag = "Status";
	
	//Properties
	private HashMap<User, UserStatus>	users;
	private User 						host;
	private Calendar					startTime;
	protected PubLocation				pubLocation;
	private int 						globalEventId;
	private EventStatus 				status;
	
	//Constructors
	public PubEvent(Calendar startTime, User host)
	{
		users = new HashMap<User, UserStatus>();
		//users.put(host, new UserStatus(GoingStatus.going, startTime, null));
		this.host = host;
		this.startTime = startTime;
		globalEventId = Constants.EventIdNotAssigned;
		status = EventStatus.unknown;
	}
	
	public PubEvent(Calendar startTime, PubLocation pubLocation, User host)
	{
		users = new HashMap<User, UserStatus>();
		users.put(host, new UserStatus(GoingStatus.going, startTime, null));
		this.host = host;
		this.pubLocation = pubLocation;
		this.startTime = startTime;
		globalEventId = Constants.EventIdNotAssigned;
		status = EventStatus.unknown;
	}
	
	public PubEvent(Element element)
	{
		readXml(element);
	}
	
	//Getter/setter methods
	public Set<User> GetUsers()
	{
		return users.keySet();
	}
	
	public User[] GetUserArray()
	{
		return users.keySet().toArray(new User[users.size()]);
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
	
	public EventStatus getCurrentStatus()
	{
		return status;
	}
	
	public void setCurrentStatus(EventStatus status)
	{
		this.status = status;
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
	
	
	public String toString()
	{
		return pubLocation.getName() + " : " + GetFormattedStartTime();
	}
	
	public String GetFormattedStartTime()
	{
		return PubEvent.GetFormattedDate(startTime);
	}
	
	public static String GetFormattedDate(Calendar calendar)
	{
		String time;
		
		String date;
		Calendar currentTime = Calendar.getInstance();
		if(calendar.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH))
		{
			date = "Today";
		}
		else 
		{
			currentTime.add(Calendar.DAY_OF_MONTH, 1);
			if(currentTime.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
			{
				date = "Tomorrow";
			}
			else if(calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
			{
				//Same year - don't bother with year
				date = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1);
			}
			else
			{
				date = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + calendar.get(Calendar.YEAR);
			}
		}
		String minTime = Integer.toString(calendar.get(Calendar.MINUTE));
		if(calendar.get(Calendar.MINUTE) < 10)
		{
			minTime = "0" + minTime;
		}
		time = date + " at " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + minTime;
		
		return time;	
	}
	
	public Element writeXml()
	{
		Element pubEventElement = new Element(getClass().getSimpleName());
		
		Element eventIdElement = new Element(eventIdTag);
		eventIdElement.setText(Integer.toString(globalEventId));
		pubEventElement.addContent(eventIdElement);
		
		Element startTimeElement = new Element(startTimeTag);
		startTimeElement.setText(Long.toString(startTime.getTimeInMillis()));
		pubEventElement.addContent(startTimeElement);
		
		pubEventElement.addContent(pubLocation.writeXml());
		
		Element statusElement = new Element(statusTag);
		statusElement.setText(status.toString());
		pubEventElement.addContent(statusElement);
		
		Element hostElement = new Element(hostTag);
		hostElement.addContent(host.writeXmlForTransmission());
		pubEventElement.addContent(hostElement);
		
		Element usersElement = new Element(usersTag);
		for(Entry<User, UserStatus> userEntry : users.entrySet())
		{
			Element invitedUserElement = new Element(invitedUserTag);
			
			invitedUserElement.addContent(userEntry.getKey().writeXmlForTransmission());
			invitedUserElement.addContent(userEntry.getValue().writeXml());
			
			usersElement.addContent(invitedUserElement);
		}
		pubEventElement.addContent(usersElement);
		
		return pubEventElement;
	}
	
	public void readXml(Element element)
	{
		globalEventId = Integer.parseInt(element.getChildText(eventIdTag));
		
		startTime = Calendar.getInstance();
		startTime.setTimeInMillis(Long.parseLong(element.getChildText(startTimeTag)));
		
		pubLocation = new PubLocation(element.getChild(PubLocation.class.getSimpleName()));
		
		status = EventStatus.valueOf(element.getChild(statusTag).getText());
		
		host = new User(element.getChild(hostTag).getChild(User.class.getSimpleName()));
		
		users = new HashMap<User, UserStatus>();
		List<Element> invitedElements = element.getChild(usersTag).getChildren(invitedUserTag);
		for(Element invitedUserElement : invitedElements)
		{
			User user = new User(invitedUserElement.getChild(User.class.getSimpleName()));
			UserStatus status = new UserStatus(invitedUserElement.getChild(UserStatus.class.getSimpleName()));
			
			users.put(user, status);
		}
	}

	public void emptyGuestList() {
		users = new HashMap<User, UserStatus>();
		users.put(host, new UserStatus(GoingStatus.going, startTime, null));
	}
	
	
	public int hashCode()
	{
		return globalEventId;
	}
}










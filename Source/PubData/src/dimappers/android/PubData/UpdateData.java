package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

public class UpdateData implements Serializable, IXmlable {

	private static final String StartTimeTag = "StartTime";
	private static final String UsersTag = "Users";
	private static final String EventIdTag = "EventId";
	
	private Calendar					startTime;
	private PubLocation 				pubLocation;
	private LinkedList<User>			users;
	private int 						eventId;
	
	public UpdateData(int eventId, Calendar startTime, PubLocation pubLocation) {
		this.startTime = 	startTime;
		this.pubLocation = 	pubLocation;
		this.users = 		new LinkedList<User>();
		this.eventId = eventId;
	}
	
	public UpdateData(Element element)
	{
		readXml(element);
	}
	
	public void addUser(User user) {
		users.add(user);
	}
	
	public int getEventId() 						{ return this.eventId; }
	public Calendar getStartTime() 					{ return this.startTime; }
	public PubLocation getPubLocation() 			{ return this.pubLocation; }
	public LinkedList<User> getUsers()	 			{ return this.users; }
	
	public Element writeXml()
	{
		Element updateElement = new Element(getClass().getSimpleName());
		
		Element eventIdElement = new Element(EventIdTag);
		eventIdElement.addContent(Integer.toString(eventId));
		updateElement.addContent(eventIdElement);
		
		Element startTimeElement = new Element(StartTimeTag);
		startTimeElement.addContent(Long.toString(startTime.getTimeInMillis()));
		updateElement.addContent(startTimeElement);
		
		updateElement.addContent(pubLocation.writeXml());
		
		Element usersElement = new Element(UsersTag);
		for(User user : users)
		{
			usersElement.addContent(user.writeXmlForTransmission());
		}
		updateElement.addContent(usersElement);
		
		return updateElement;
	}
	
	public void readXml(Element element)
	{
		startTime = Calendar.getInstance();
		startTime.setTimeInMillis(Long.parseLong(element.getChildText(StartTimeTag)));
		
		Element usersElement = element.getChild(UsersTag);
		List<Element> userElements = usersElement.getChildren(User.class.getSimpleName());
		users = new LinkedList<User>();
		for(Element userElement : userElements)
		{
			users.add(new User(userElement));
		}
		
		pubLocation = new PubLocation(element.getChild(PubLocation.class.getSimpleName()));
		eventId = Integer.parseInt(element.getChildText(EventIdTag));
	}
}

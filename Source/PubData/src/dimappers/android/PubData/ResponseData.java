package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Calendar;

import org.jdom.Element;

public class ResponseData implements Serializable, IXmlable
{
	private static final String eventIdTag = "EventId";
	private static final String isGoingTag = "IsGoing";
	private static final String freeFromTag = "FreeFrom";
	private static final String msgTag = "Msg";
	
	private User user; //Who is responding
	private int eventId;
	private boolean isGoing;
	private Calendar freeFromWhen;
	private String msgToHost; 
	
	public ResponseData(User user, int eventId, boolean isGoing)
	{
		this.user = user;
		this.eventId = eventId;
		this.isGoing = isGoing;
		
		msgToHost = "";
	}
	
	public ResponseData(User user, int eventId, boolean isGoing, Calendar freeFromWhen, String msgToHost)
	{
		this(user, eventId, isGoing);
		this.freeFromWhen = freeFromWhen;
		this.msgToHost = msgToHost;
	}
	
	public ResponseData(Element element)
	{
		readXml(element);
	}
	
	public User GetUser()
	{
		return user;
	}
	
	public int GetEventId()
	{
		return eventId;
	}
	
	public boolean GetIsGoing()
	{
		return isGoing;
	}
	
	public Calendar GetFreeFromWhen()
	{
		return freeFromWhen;
	}
	
	public String GetMsgToHost()
	{
		return msgToHost;
	}
	
	public UserStatus MakeUserStatus()
	{
		GoingStatus goingStatus;
		if(isGoing)
		{
			goingStatus = GoingStatus.going;
		}
		else
		{
			goingStatus = GoingStatus.notGoing;
		}
		return new UserStatus(goingStatus, freeFromWhen, msgToHost);
	}
	
	public Element writeXml()
	{
		Element responseElement = new Element(getClass().getSimpleName());
		
		responseElement.addContent(user.writeXmlForTransmission());
		Element eventIdElement = new Element(eventIdTag);
		eventIdElement.addContent(Integer.toString(eventId));
		responseElement.addContent(eventIdElement);
		Element isGoingElement = new Element(isGoingTag);
		isGoingElement.addContent(Boolean.toString(isGoing));
		responseElement.addContent(isGoingElement);
		
		if(freeFromWhen != null)
		{
			Element freeFromWhenElement = new Element(freeFromTag);
			freeFromWhenElement.addContent(Long.toString(freeFromWhen.getTimeInMillis()));
			responseElement.addContent(freeFromWhenElement);
		}
		
		if(msgToHost != null && msgToHost != "")
		{
			Element messageElement = new Element(msgTag);
			messageElement.addContent(msgToHost);
			responseElement.addContent(messageElement);
		}
		
		return responseElement;
	}
	
	public void readXml(Element element)
	{
		user = new User(element.getChild(User.class.getSimpleName()));
		eventId = Integer.parseInt(element.getChildText(eventIdTag));
		isGoing = Boolean.parseBoolean(element.getChildText(isGoingTag));
		
		Element calElement = element.getChild(freeFromTag);
		if(calElement != null)
		{
			freeFromWhen = Calendar.getInstance();
			freeFromWhen.setTimeInMillis(Long.parseLong(calElement.getText()));
		}
		
		Element msgElement = element.getChild(msgTag);
		if(msgElement != null)
		{
			msgToHost = msgElement.getText();
		}
	}
}

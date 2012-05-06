package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Calendar;

import org.jdom.Element;

public class UserStatus implements Serializable
{
	private final String freeFromTag = "FreeFrom";
	private final String goingTag = "GoingStatus";
	private final String msgTag = "Msg";

	
	public GoingStatus goingStatus;
	public Calendar freeFrom;
	public String messageToHost;
	
	public UserStatus(GoingStatus goingStatus, Calendar freeFrom, String messageToHost)
	{
		this.goingStatus = goingStatus;
		this.freeFrom = freeFrom;
		this.messageToHost = messageToHost;
	}
	
	public UserStatus(Element element)
	{
		readXml(element);
	}
	
	public Element writeXml()
	{
		Element userStatusElement = new Element(getClass().getSimpleName());
		
		Element freeFromElement = new Element(freeFromTag);
		freeFromElement.addContent(Long.toString(freeFrom.getTimeInMillis()));
		userStatusElement.addContent(freeFromElement);
		
		Element goingElement = new Element(goingTag);
		goingElement.addContent(goingStatus.toString());
		userStatusElement.addContent(goingElement);
		
		Element messageElement = new Element(msgTag);
		messageElement.addContent(messageToHost);
		userStatusElement.addContent(messageElement);
		
		return userStatusElement;
	}
	
	public void readXml(Element element)
	{
		goingStatus = GoingStatus.valueOf(element.getChildText(goingTag));
		freeFrom = Calendar.getInstance();
		freeFrom.setTimeInMillis(Long.parseLong(element.getChildText(freeFromTag)));
		Element msgElement = element.getChild(msgTag);
		if(msgElement != null)
		{
			messageToHost = msgElement.getText();
			System.out.println("HELLO");
		}
		else
		{
			messageToHost = "";
		}
	}
}

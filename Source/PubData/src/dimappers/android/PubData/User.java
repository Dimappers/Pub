package dimappers.android.PubData;

import java.io.Serializable;

import org.jdom.Element;


/*
 * This class holds data about the User. It should be overridden in the app and the 
 * server to extend functionality. 
 * The server will contain the id's of the events the user is a part of.
 *
 * Author: MF
 */
public class User implements Serializable {
	
	//Properties
	protected Long facebookUserId;
	
	//Constructor
	public User(Long facebookUserId) {
		this.facebookUserId = facebookUserId;
	}
	
	public User(Element userXmlElement)
	{
		readXmlForTransmission(userXmlElement);
	}
	
	
	//Encapsulation
	public Long getUserId() 	{ return facebookUserId; }	
	
	@Override
	public boolean equals(Object otherUser)
	{
		if(otherUser instanceof User)
		{
			return facebookUserId.equals(((User)otherUser).getUserId());
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return facebookUserId.hashCode();
	}
	
	public Element writeXmlForTransmission()
	{
		/*At the moment just adds a <User>123</User> tag to reduce space - could be 
		 * <User>
		 * 		<UserId>123</UserId>
		 * </User>
		 * If we have to add more information
		 */
		
		Element user = new Element("User");
		user.addContent(facebookUserId.toString());
		
		return user;
	}
	
	public void readXmlForTransmission(Element userXmlElement)
	{
		facebookUserId = Long.parseLong(userXmlElement.getText());
	}

	@Override
	public String toString()
	{
		return facebookUserId.toString();
	}
}

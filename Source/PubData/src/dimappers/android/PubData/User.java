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
	private Long facebookUserId;
	private float latitude = 1000.0f;
	private float longitude = 1000.0f;
	private int rank = 0;
	
	//Constructor
	public User(Long facebookUserId) {
		this.facebookUserId = facebookUserId;
	}
	
	public User(Element userXmlElement)
	{
		readXml(userXmlElement);
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
	
	public double[] getLocation()
	{
		if(longitude!=1000.0f&&latitude!=1000.0f)
		{
			double[] returnValue = new double[2];
			returnValue[0] = longitude;
			returnValue[1] = latitude;
			return returnValue;
		}
		else return null;
	}
	
	public void setRank(int rank) {this.rank = rank;}
	public int getRank() {return rank;}
	
	@Override
	public int hashCode()
	{
		return facebookUserId.hashCode();
	}
	
	public Element writeXml()
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
	
	public void readXml(Element userXmlElement)
	{
		facebookUserId = Long.parseLong(userXmlElement.getText());
	}

	@Override
	public String toString()
	{
		return facebookUserId.toString();
	}
}

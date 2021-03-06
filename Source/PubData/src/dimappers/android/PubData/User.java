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
	private int rank = 0;
	
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
	
	public void setRank(int rank) {this.rank = rank;}
	public int getRank() {return rank;}
	
	
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
		
		user.setText(facebookUserId.toString());

		return user;
	}
	
	public void readXmlForTransmission(Element userXmlElement)
	{
		facebookUserId = Long.parseLong(userXmlElement.getText());
	}

	
	public String toString()
	{
		return facebookUserId.toString();
	}

	//For breaking down how the rank is calculated
	public int PostsFromWho = 0;
	public int PostsTagged = 0;
	public int PostsLiked = 0;
	public int PostsWithYou = 0;
	public int PostsComments = 0;
	public int PostsTaggedInComment = 0;
	public int PhotosFromWho = 0;
	public int PhotosTagged = 0;
	public int History = 0;
	public int PhotosLiked = 0;
	public int PhotosComments = 0;
	public int CallLogTotal = 0;

}

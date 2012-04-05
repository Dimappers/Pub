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
	private double latitude = 1000.0;
	private double longitude = 1000.0;
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
			returnValue[0] = latitude;
			returnValue[1] = longitude;
			return returnValue;
		}
		else return null;
	}
	
	public void setLocation(double[] location)
	{
		if(location.length==2)
		{
			latitude = location[0];
			longitude = location[1];
		}
	}
	
	public void setRank(int rank) {this.rank = rank;}
	public int getRank() {return rank;}
	
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
		user.setText(facebookUserId.toString());
		
		//When we start actually using the location, need to uncomment this bit & rewriting above bit to include new element
		/*if(latitude!=1000.0||longitude!=1000.0)
		{
			Element locElem = new Element("location");
			locElem.addContent(new Element("latitude").setText(""+latitude));
			locElem.addContent(new Element("longitude").setText(""+longitude));
			
			user.addContent(locElem);
		}*/
		return user;
	}
	
	public void readXmlForTransmission(Element userXmlElement)
	{
		facebookUserId = Long.parseLong(userXmlElement.getText());
		
		//See comment in write method above
		/*if(userXmlElement.getChild("location")!=null)
		{
			Element locElem = userXmlElement.getChild("location");
			latitude = Double.parseDouble(locElem.getChildText("latitude"));
			longitude = Double.parseDouble(locElem.getChildText("longitude"));
		}*/
		
	}

	@Override
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

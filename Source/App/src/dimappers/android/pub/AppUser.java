package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;

import org.jdom.Element;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.User;

public class AppUser extends User implements IXmlable
{
	private final String usernameTag = "UserName";
	final static String outOfDateTag = "outOfDate";
	final static String rankTag = "rank";
	
	private Calendar outOfDate;
	
	//Properties
	private String facebookName;
	
	private double latitude = 1000.0;
	private double longitude = 1000.0;
	private String locationName;
	
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
	
	public void updateLocation(double[] location)
	{
		if(latitude==1000.0||longitude==1000.0)
		{
			setLocation(location);
		}
		else
		{
			latitude += location[0];
			latitude /= 2;
			longitude += location[1];
			longitude /= 2;
		}
	}
	
	public String getLocationName() {return locationName;}
	public void setLocationName(String loc) {locationName = loc;}

	//Constructors
	public AppUser(Long facebookUserId)
	{
		super(facebookUserId);
		outOfDate = Calendar.getInstance();
		outOfDate.add(Calendar.DATE,Constants.AppUserOutOfDateTime);
	}
	
	public AppUser(Long facebookUserId, String name)
	{
		super(facebookUserId);
		facebookName = name;
		outOfDate = Calendar.getInstance();
		outOfDate.add(Calendar.DATE,Constants.AppUserOutOfDateTime);
	}
	
	public AppUser(Element element)
	{
		super(element.getChild("User"));
		readXml(element);
	}
	
	
	public String toString()
	{
		return facebookName;
	}
	
	
	public String getName()
	{
		return facebookName;
	}
	
	public Element writeXml()
	{	
		Element userElement = new Element("AppUser");
		userElement.addContent(super.writeXmlForTransmission()); //we write the user id & location in this part of the xml
		
		Element userNameElement = new Element(usernameTag);
		userNameElement.setText(facebookName);
		userElement.addContent(userNameElement);
		
		userElement.addContent(new Element(outOfDateTag).setText(new Long(outOfDate.getTimeInMillis()).toString()));
		
		userElement.addContent(new Element(rankTag).setText(""+getRank()));
		
		if(latitude!=1000.0||longitude!=1000.0) //only need to include the location if it has been set (1000.0 is the default)
		{
			Element locElem = new Element("location");
			locElem.addContent(new Element("latitude").setText(""+latitude));
			locElem.addContent(new Element("longitude").setText(""+longitude));
			
			userElement.addContent(locElem);
		}
		
		return userElement;
	}
	
	
	public void readXml(Element userXmlElement)
	{
		if(facebookUserId == null || facebookUserId == 0)
		{
			Log.d(Constants.MsgError, "Must call parent read first! - use the constructor for AppUser");
		}
		
		facebookName = userXmlElement.getChildText(usernameTag);
		
		outOfDate = Calendar.getInstance();
		outOfDate.setTime(new Date(Long.parseLong(userXmlElement.getChildText(outOfDateTag))));
		
		setRank(Integer.parseInt(userXmlElement.getChildText(rankTag)));
		
		if(userXmlElement.getChild("location")!=null)
		{
			Element locElem = userXmlElement.getChild("location");
			latitude = Double.parseDouble(locElem.getChildText("latitude"));
			longitude = Double.parseDouble(locElem.getChildText("longitude"));
		}
		else
		{
			latitude = 1000.0;
			longitude = 1000.0;
		}
	}

	
	//Public methods
	
	//Get facebook profile picture
	/*Images GetFacebookImage()
	{
		return null;		
	}
	
	//Get full facebook name, ie Thomas Kiley instead of thomas.kiley
	String GetRealFacebookName()
	{
		return getUserId().toString();
	}
	
	
	public String toString()
	{
		return "Id: " + GetRealFacebookName();		
	}
	*/
	public void setOutOfDate(Calendar newTime)
	{
		outOfDate = newTime;
	}
	
	public boolean isOutOfDate()
	{
		return Calendar.getInstance().after(outOfDate);
	}
	
	public static AppUser AppUserFromUser(User user, Facebook facebook) throws MalformedURLException, JSONException, IOException
	{
		if(user instanceof AppUser) { return (AppUser) user; }
		JSONObject them;
		them = new JSONObject(facebook.request(Long.toString(user.getUserId())));
		Calendar current = Calendar.getInstance();
		current.add(Calendar.DATE,Constants.AppUserOutOfDateTime);
		AppUser createdAppUser = new AppUser(user.getUserId(), them.getString("name"));
		createdAppUser.setOutOfDate(current);
		return createdAppUser;
	}
}

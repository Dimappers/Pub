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
	
	private Calendar outOfDate;
	
	//Properties
	private String facebookName;

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
	
	public Element writeXml()
	{	
		Element userElement = new Element("AppUser");
		userElement.addContent(super.writeXmlForTransmission()); //we write the user id in this part of the xml
		Element userNameElement = new Element(usernameTag);
		userNameElement.setText(facebookName);
		userElement.addContent(userNameElement);
		
		userElement.addContent(new Element(outOfDateTag).setText(new Long(outOfDate.getTimeInMillis()).toString()));
		
		return userElement;
	}
	
	public void readXml(Element userXmlElement)
	{
		//facebookUserId = Long.parseLong(userXmlElement.getText());
		if(facebookUserId == null || facebookUserId == 0)
		{
			Log.d(Constants.MsgError, "Must call parent read first! - use the constructor for AppUser");
		}
		
		facebookName = userXmlElement.getChildText(usernameTag);
		
		outOfDate = Calendar.getInstance();
		outOfDate.setTime(new Date(Long.parseLong(userXmlElement.getChildText(outOfDateTag))));
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
	
	@Override
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
		JSONObject them;
		them = new JSONObject(facebook.request(Long.toString(user.getUserId())));
		Calendar current = Calendar.getInstance();
		current.add(Calendar.DATE,Constants.AppUserOutOfDateTime);
		AppUser createdAppUser = new AppUser(user.getUserId(), them.getString("name"));
		createdAppUser.setOutOfDate(current);
		return createdAppUser;
	}
}

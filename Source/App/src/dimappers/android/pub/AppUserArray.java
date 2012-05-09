package dimappers.android.pub;

import java.util.Calendar;
import java.util.List;

import org.jdom.Element;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.IXmlable;

public class AppUserArray implements IXmlable {
	
	private final String lastUpdateTag = "LastUpdate";
	private final String friendsTag = "Friends";
	
	private AppUser[] internalArray;
	
	private Calendar lastUpdated;
	
	
	public AppUserArray(AppUser[] array)
	{
		internalArray = array;
		lastUpdated = Calendar.getInstance();
	}
	
	public void setArray(AppUser[] array)
	{
		internalArray = array;
		lastUpdated = Calendar.getInstance();
	}
	
	public AppUser[] getArray()
	{
		return internalArray;
	}
	
	public boolean isOutOfDate()
	{
		Calendar weekAfterUpdate = Calendar.getInstance();
		weekAfterUpdate.setTime(lastUpdated.getTime());
		weekAfterUpdate.add(Calendar.DAY_OF_MONTH, Constants.FriendsOutOfDateTime);
		return Calendar.getInstance().after(weekAfterUpdate); //if we are after a week after the last update, we need updating
	}
	
	@Override
	public Element writeXml() {
		Element rootElement = new Element(getClass().getSimpleName());
		
		Element lastUpdatedElement = new Element(lastUpdateTag);
		lastUpdatedElement.addContent(Long.toString(lastUpdated.getTimeInMillis()));
		rootElement.addContent(lastUpdatedElement);
		
		Element friendsElement = new Element(friendsTag);
		for(AppUser friend : internalArray)
		{
			friendsElement.addContent(friend.writeXml());
		}
		rootElement.addContent(friendsElement);
		
		return rootElement;
	}

	@Override
	public void readXml(Element element) {
		lastUpdated = Calendar.getInstance();
		lastUpdated.setTimeInMillis(Long.parseLong(element.getChildText(lastUpdateTag)));
		
		List<Element> friendsElements = element.getChild(friendsTag).getChildren(AppUser.class.getSimpleName());
		
		for(Element friendElement : friendsElements)
		{
			AppUser friend = new AppUser(friendElement);
		}
	}

}

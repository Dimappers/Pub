package dimappers.android.PubData;

import java.io.Serializable;

import org.jdom.Element;

public class RefreshData implements Serializable, IXmlable {

	private static final String fullupdateTag = "FullUpdate";
	
	private User 	user;
	private boolean fullUpdate;
	
	//Constructor
	public RefreshData(User user, boolean fullUpdate) {
		this.user 	= user;		// The users unique id (facebook name) + authentication data
		this.fullUpdate = fullUpdate;	// If True, needs a full update
	}
	
	public RefreshData(Element element)
	{
		readXml(element);
	}
	
	//Encapsulation
	public User 	getUser() 		{ return user; }
	public Long 	getUserId()		{ return user.getUserId(); }
	public boolean 	isFullUpdate() 	{ return fullUpdate; }
	
	public Element writeXml()
	{
		Element refreshElement = new Element(getClass().getSimpleName());
		
		refreshElement.addContent(user.writeXmlForTransmission());
		Element fullUpdateElement = new Element(fullupdateTag);
		fullUpdateElement.addContent(Boolean.toString(fullUpdate));
		refreshElement.addContent(fullUpdateElement);
		
		return refreshElement;
	}
	
	public void readXml(Element element)
	{
		user = new User(element.getChild(User.class.getSimpleName()));
		fullUpdate = Boolean.parseBoolean(element.getChildText(fullupdateTag));
	}
}

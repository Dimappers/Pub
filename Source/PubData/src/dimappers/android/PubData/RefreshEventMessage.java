package dimappers.android.PubData;

import org.jdom.Element;

public class RefreshEventMessage implements IXmlable {

	private final String eventIdTag = "id";
	
	int eventId;
	User user;
	
	public RefreshEventMessage(int id, User user)
	{
		eventId = id;
		this.user = user;
	}
	
	public RefreshEventMessage(Element element)
	{
		readXml(element);
	}
	
	public int getEventId()
	{
		return eventId;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public Element writeXml() {
		Element root = new Element(this.getClass().getSimpleName());
	
		root.addContent(user.writeXmlForTransmission());
		
		Element eventIdElement = new Element(eventIdTag);
		eventIdElement.setText(Integer.toString(eventId));
		root.addContent(eventIdElement);
		
		return root;
	}

	public void readXml(Element element) {
		user = new User(element.getChild(User.class.getSimpleName()));
		eventId = Integer.parseInt(element.getChildText(eventIdTag));
	}

}

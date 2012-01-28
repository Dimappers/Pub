package dimappers.android.PubData;

import java.io.Serializable;

public class ResponseData implements Serializable
{
	private User user; //Who is responding
	private int eventId;
	private boolean isGoing;
	
	public ResponseData(User user, int eventId, boolean isGoing)
	{
		this.user = user;
		this.eventId = eventId;
		this.isGoing = isGoing;
	}
	
	public User GetUser()
	{
		return user;
	}
	
	public int GetEventId()
	{
		return eventId;
	}
	
	public boolean GetIsGoing()
	{
		return isGoing;
	}
}

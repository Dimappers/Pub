package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Calendar;

public class ResponseData implements Serializable
{
	private User user; //Who is responding
	private int eventId;
	private boolean isGoing;
	private Calendar freeFromWhen;
	private String msgToHost; 
	
	public ResponseData(User user, int eventId, boolean isGoing)
	{
		this.user = user;
		this.eventId = eventId;
		this.isGoing = isGoing;
		
		msgToHost = "";
	}
	
	public ResponseData(User user, int eventId, boolean isGoing, Calendar freeFromWhen, String msgToHost)
	{
		this(user, eventId, isGoing);
		this.freeFromWhen = freeFromWhen;
		this.msgToHost = msgToHost;
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
	
	public Calendar GetFreeFromWhen()
	{
		return freeFromWhen;
	}
	
	public String GetMsgToHost()
	{
		return msgToHost;
	}
	
	public UserStatus MakeUserStatus()
	{
		GoingStatus goingStatus;
		if(isGoing)
		{
			goingStatus = GoingStatus.going;
		}
		else
		{
			goingStatus = GoingStatus.notGoing;
		}
		return new UserStatus(goingStatus, freeFromWhen, msgToHost);
	}
}

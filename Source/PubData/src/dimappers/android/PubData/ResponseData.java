package dimappers.android.PubData;

public class ResponseData
{
	private User guest; //Who is responding
	private int eventId;
	private boolean isGoing;
	
	public ResponseData(User guest, int eventId, boolean isGoing)
	{
		this.guest = guest;
		this.eventId = eventId;
		this.isGoing = isGoing;
	}
	
	public User GetGuest()
	{
		return guest;
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

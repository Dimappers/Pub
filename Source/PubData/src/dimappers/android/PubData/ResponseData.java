package dimappers.android.PubData;

public class ResponseData
{
	private Guest guest; //Who is responding
	private int eventId;
	private boolean isGoing;
	
	public ResponseData(Guest guest, int eventId, boolean isGoing)
	{
		this.guest = guest;
		this.eventId = eventId;
		this.isGoing = isGoing;
	}
	
	public Guest GetGuest()
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

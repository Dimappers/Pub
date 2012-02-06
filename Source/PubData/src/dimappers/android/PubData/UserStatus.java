package dimappers.android.PubData;

import java.io.Serializable;
import java.util.Calendar;

public class UserStatus implements Serializable
{
	public GoingStatus goingStatus;
	public Calendar freeFrom;
	
	public String messageToHost;
	
	public UserStatus(GoingStatus goingStatus, Calendar freeFrom, String messageToHost)
	{
		this.goingStatus = goingStatus;
		this.freeFrom = freeFrom;
		this.messageToHost = messageToHost;
	}
}

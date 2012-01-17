package dimappers.android.server;

import java.util.HashMap;
import java.util.LinkedList;

import dimappers.android.PubData.Guest;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubTripState;

public class ServerGuest extends dimappers.android.PubData.Guest 
{
	private boolean hasApp;
	
	public ServerGuest(String facebookUserName) {
		super(facebookUserName);
		hasApp = false;
	}
	
	public boolean GetHasApp()
	{
		return hasApp;
	}
	public void SetHasApp(boolean hasApp)
	{
		this.hasApp = hasApp;
	}
}

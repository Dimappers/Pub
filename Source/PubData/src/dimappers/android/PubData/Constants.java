package dimappers.android.PubData;

public final class Constants {
	//Common bundle tags
	public static final String CurrentWorkingEvent = "event";
	public static final String CurrentFacebookUser = "facebookId";
	public static final String NewEventFlag = "NewEvent";
	
	//Error constants
	public static final int MissingDataInBundle = -12;
	
	//Launch application return codes
	public static final int FromPending = 0;
	public static final int FromOrganise = 1;
	
	//Organise return codes
	public static final int PubLocationReturn = 1;
	public static final int GuestReturn = 2;
	public static final int StartingTimeReturn = 3;
	
	//Events Array Positions
	public static final int NewEventNoResponse = 0;
	public static final int HostedEventSent = 1;
	public static final int NewEventHaveResponded = 2;
	public static final int HostedEventSaved = 3;
	public static final int NumberOfEventCategories = 4;
	
	//Server stuff
	public static final String ServerIp = "127.0.0.1";
	public static final int Port = 2085;
	
}

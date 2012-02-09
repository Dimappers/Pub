package dimappers.android.PubData;

public final class Constants {
	//Common bundle tags
	public static final String CurrentWorkingEvent = "event";
	public static final String CurrentFacebookUser = "facebookId";
	public static final String IsSavedEventFlag = "NewEvent";
	public static final String CurrentLatitude = "lat";
	public static final String CurrentLongitude = "long";
	
	//Error constants
	public static final int MissingDataInBundle = -12;
	
	//Launch application return codes
	public static final int FromPending = 0;
	public static final int FromOrganise = 1;
	public static final int NoInternet = 2;
	
	//Organise return codes
	public static final int PubLocationReturn = 1;
	public static final int GuestReturn = 2;
	public static final int StartingTimeReturn = 3;
	
	//Events Array Positions
	public static final int ProposedEventNoResponse = 0;
	public static final int HostedEventSent = 1;
	public static final int ProposedEventHaveResponded = 2;
	public static final int HostedEventSaved = 3;
	public static final int NumberOfEventCategories = 4;
	
	//Server stuff
	public static final String ServerIp = "127.0.0.1";
	public static final int Port = 2085;
	
	//Log categories
	public static final String MsgInfo = "Dimap:Info";
	public static final String MsgWarning = "Dimap:Warning";
	public static final String MsgError = "Dimap:Error";
	
	public static final String SaveDataName = "PubStore";
}

package dimappers.android.PubData;

public final class Constants {
	// Debugging mode for server
	public static final boolean debug = true;
	public static final boolean emulator = true;
	
	//Common bundle tags
	public static final String CurrentWorkingEvent = "event";
	public static final String CurrentFacebookUser = "facebookId";
	public static final String IsSavedEventFlag = "NewEvent";
	public static final String CurrentLatitude = "lat";
	public static final String CurrentLongitude = "long";
	
	//Error constants
	public static final int MissingDataInBundle = -12;
	public static final int ErrorEventId = -384051;
	public static final int EventIdNotAssigned = -1;
	
	//Launch application return codes
	public static final int FromPending = 0;
	public static final int FromOrganise = 1;
	public static final int NoInternet = 2;
	
	//HostEvent return codes
	public static final int FromEdit = 0;
	
	//Pending screen progress update codes
	public static final Integer CreatingEvent = new Integer(0);
	public static final Integer ChoosingPub = new Integer(1);
	public static final Integer PickingGuests = new Integer(2);
<<<<<<< HEAD

=======
>>>>>>> 285ffae13f75020fbf5fac692aed6426171dbf8b
	
	public static final Integer PubFinderError = new Integer(0);
	public static final Integer PubFinderOK = new Integer(1);
	
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
	public static final String ServerIp = "81.111.109.139";
	public static final int Port = 8031;
	
	//Log categories
	public static final String MsgInfo = "Dimap:Info";
	public static final String MsgWarning = "Dimap:Warning";
	public static final String MsgError = "Dimap:Error";
	
	public static final String SaveDataName = "PubStore";
}

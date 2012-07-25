package dimappers.android.PubData;

public final class Constants {
	// Debugging mode for server
	public static final boolean debug = true;
	public static final boolean emulator = false;
	
	//Common bundle tags
	public static final String CurrentWorkingEvent = "event"; //IMPORTANT: This should just pass the event id1
	public static final String CurrentFacebookUser = "facebookId";
	public static final String IsSavedEventFlag = "NewEvent";
	public static final String CurrentLatitude = "lat";
	public static final String CurrentLongitude = "long";
	public static final String AuthToken = "authtoken";
	public static final String Expires = "expires";
	public static final String FacebookAppId = "153926784723826";
	public static final String RequiredNotificationType = "requiredNotificationType";
	
	//Error constants
	public static final int MissingDataInBundle = -12;
	public static final int ErrorEventId = -384051;
	public static final int EventIdBeingSent = -1001; //Event id for event that is being sent but has not received its ID back
	public static final int EventIdNotAssigned = -1;
	
	//Launch application return codes
	public static final int FromPending = 0;
	public static final int FromOrganise = 1;
	public static final int NoInternet = 2;
	public static final int FromFacebookLogin = 3;
	
	//HostEvent return codes
	public static final int FromEdit = 0;
	
	//Pending screen progress update codes
	public static final Integer CreatingEvent = new Integer(0);
	public static final Integer ChoosingPub = new Integer(1);
	public static final Integer PickingGuests = new Integer(2);
	
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
	public static final String ServerIp = "176.34.191.200";
	public static final int Port = 8031;
	
	//Log categories
	public static final String MsgInfo = "Dimap:Info";
	public static final String MsgWarning = "Dimap:Warning";
	public static final String MsgError = "Dimap:Error";
	
	public static final String SaveDataName = "PubStore";
	
	//Google URL details
	public final static String API_KEY = "AIzaSyBg0eJlYa_70fG8dc1xdHKFT3BoEWwEQ6M";
	public final static String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
	public final static String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	public static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
	
	public static final String NoDictionaryForGenericDataStore = "NoDictionary";
	
	//Amount of time until each element goes out of date
	public static final int PubOutOfDateTime = 17;
	public static final int AppUserOutOfDateTime = 10;
	public static final int CurrentLocationOutOfDateTime = 15;
	public static final int XmlObjectOutOfDateTime = 7; //TODO: May want to change this, so each different type of XMlJasonObject has a different outofdate time
	public static final int FriendsOutOfDateTime = 7;
	
	public static final int MessageTimeSetter = 400;
	public static final String HostOrNot = "host?";
	public static final String ChosenTime = "timechosenbyguest";
	
	//Broadcast names - NOTE: if changed, should also update the names in AndroidManifest	
	public static final String broadcastDeleteString = "dimappers.android.pub.deleteEvent";
	public static final String broadcastReminderString = "dimappers.android.pub.eventReminder";
	public static final String broadcastConfirmReminderString = "dimappers.android.pub.confirmEventReminder";

}

package dimappers.android.PubData;

/* This class holds information about a guest
 * It does nothing with this data, it is purely a data store
 * 
 * This class should be overridden in both the App and the Server
 * 	In the app can contain extra GUI/Facebook stuff like profile pic, real name
 * 	In the server it should contain details of how to message the person (eg do they have the app)
 * 
 * Author: TK
 */
public abstract class Guest
{
	//Properties
	private String 			facebookUserName;
	private PubTripState	isGoingToThePub;
	
	//Constructors
	public Guest(String facebookUserName)
	{
		this.facebookUserName = facebookUserName;
		isGoingToThePub = PubTripState.MightGo;
	}
	
	public Guest(String facebookUserName, boolean hasAppInstalled, PubTripState isGoingToThePub)
	{
		this.facebookUserName = facebookUserName;
		this.isGoingToThePub = isGoingToThePub;
	}
	
	//Getter/Setter methods
	public String GetFacebookUserName()
	{
		return facebookUserName;
	}
	
	public PubTripState GetIsGoingToThePub()
	{
		return isGoingToThePub;
	}
	public void SetIsGoingToThePub(PubTripState isGoingToThePub)
	{
		this.isGoingToThePub = isGoingToThePub;
	}
}

enum PubTripState
{
	Going,
	MightGo,
	CantGo
}

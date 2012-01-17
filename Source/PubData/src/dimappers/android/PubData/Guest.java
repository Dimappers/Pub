package dimappers.android.PubData;

import java.io.Serializable;
import java.util.HashMap;

/* NOTE: THIS CLASS IS NOW DEFUNCT, USE THE 'USER' CLASS INSTEAD
 * (I reccomend this class get's deleted)
 * 
 * This class holds information about a guest
 * It does nothing with this data, it is purely a data store
 * 
 * This class should be overridden in both the App and the Server
 * 	In the app can contain extra GUI/Facebook stuff like profile pic, real name
 * 	In the server it should contain details of how to message the person (eg do they have the app)
 * 
 * Author: TK
 */
public class Guest implements Serializable
{
	//Properties
	private String 			facebookUserName;
	protected HashMap<PubEvent, PubTripState> events;
	
	//Constructors
	public Guest(String facebookUserName)
	{
		this.facebookUserName = facebookUserName;
		events = new HashMap<PubEvent, PubTripState>();
	}
	
	//Getter/Setter methods
	public String GetFacebookUserName()
	{
		return facebookUserName;
	}
	
	
	public HashMap<PubEvent, PubTripState> GetPubEvents()
	{
		return events;
	}
	
	//Public method
	public void AddEvent(PubEvent event)
	{
		events.put(event, PubTripState.MightGo);
	}
	
	public void DecideOnEvent(PubEvent event, PubTripState decision)
	{
		if(events.containsKey(event))
		{
			PubTripState state = events.get(event);
			state = decision;
			events.put(event, decision);
		}
		else
		{
			//shouldn't happen
		}
	}
}

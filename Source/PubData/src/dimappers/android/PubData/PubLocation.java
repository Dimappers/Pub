package dimappers.android.PubData;

import java.io.Serializable;

/* This class holds information about a pub location
 * It does nothing with this data, it is purely a data store
 * 
 * Author: TK
 */
public class PubLocation implements Serializable
{
	//Properties
	public double 			latitudeCoordinate;
	public double 			longitudeCoordinate;
	public String			pubName;

	
	@Override
	public String toString()
	{
		return pubName + ": (" + latitudeCoordinate + ", " + longitudeCoordinate +")";
	}
}

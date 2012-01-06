package dimappers.android.PubData;

import java.io.PrintWriter;
import java.io.Serializable;

/* This class holds information about a pub location
 * It does nothing with this data, it is purely a data store
 * 
 * Author: TK
 */
public class PubLocation implements Serializable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
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

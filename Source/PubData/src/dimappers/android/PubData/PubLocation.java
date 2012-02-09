package dimappers.android.PubData;

import java.io.Serializable;

/* This class holds information about a pub location
 * It does nothing with this data, it is purely a data store
 * 
 * Author: TK */
 
public class PubLocation implements Serializable
{
	//Properties
	public float 			latitudeCoordinate;
	public float 			longitudeCoordinate;
	public String			pubName;

	public PubLocation()
	{
		latitudeCoordinate = 0.0f;
		longitudeCoordinate = 0.0f;
		pubName = "Undefined location";
	}
	
	public PubLocation(float latitudeCoordinate, float longitudeCoordinate, String pubName)
	{
		this.latitudeCoordinate = latitudeCoordinate;
		this.longitudeCoordinate = longitudeCoordinate;
		this.pubName = pubName;
	}
	
	@Override
	public String toString()
	{
		return pubName + ": (" + latitudeCoordinate + ", " + longitudeCoordinate +")";
	}
	
	public boolean equals(PubLocation other)
	{
		return other.latitudeCoordinate == latitudeCoordinate && other.longitudeCoordinate == longitudeCoordinate;
	}
}


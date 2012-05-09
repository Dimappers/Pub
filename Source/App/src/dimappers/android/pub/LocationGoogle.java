package dimappers.android.pub;

import com.google.api.client.util.Key;

public class LocationGoogle {
	
	 @Key
	 double lat;
	 @Key 
	 double lng;
	 
	 @Override
	public String toString() {
		 return lat + "," + lng;
	 }
}
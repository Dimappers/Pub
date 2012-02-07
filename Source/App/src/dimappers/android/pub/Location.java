package dimappers.android.pub;

import com.google.api.client.util.Key;

public class Location {
	
	 @Key
	 double lat;
	 @Key 
	 double lng;
	 
	 public String toString() {
		 return lat + "," + lng;
	 }
}
package dimappers.android.pub;

import dimappers.android.PubData.Constants;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class LocationFinder {
	
	LocationManager locationManager;
	Location l;
	
	LocationFinder(LocationManager locationManager) {
		this.locationManager = locationManager;
	}	
	public void findLocation(final LocationListener listener) {
		if(Constants.emulator)
		{
			Location location = new Location("Test Location - Emulator");
			location.setLatitude(52.0);
			location.setLongitude(-1.5);
			listener.onLocationChanged(location);
		}
		else
		{
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(location==null) {location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);}
			if(location != null) 
			{
				listener.onLocationChanged(location); 
			}
			else
			{
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			}
		}
	}
}

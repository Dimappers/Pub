package dimappers.android.pub;

import dimappers.android.PubData.Constants;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationFinder {
	
	LocationManager locationManager;
	MyLocationListener locationListener;
	Pending p;	
	Location l;
	
	LocationFinder(Pending p) {
		this.p=p;
		locationManager = (LocationManager)p.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener(this);
	}	
	public Location findLocation() {
		if(Constants.emulator)
		{
			Location location = new Location("Hello");
			location.setLatitude(52.0);
			location.setLongitude(-1.5);
			return location;
		}
		else
		{
			Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(location != null) {return location;}
			else{locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);}
			return l;
		}
	}
	public void locationFound(Location l) {this.l=l;}
}

class MyLocationListener implements LocationListener{
	LocationFinder lf;
	MyLocationListener(LocationFinder lf) {this.lf = lf;}
	public void onLocationChanged(Location location) {makeUseOfNewLocation(location);}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
	
	//This method should find the current town from the latitude/longitude of the location
	public void makeUseOfNewLocation(Location location) {lf.locationFound(location);}
}

package dimappers.android.pub;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationFinder {
	
	LocationManager locationManager;
	MyLocationListener locationListener;
	Pending p;	
	LocationFinder(Pending p) {
		this.p=p;
		locationManager = (LocationManager)p.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener(p);
	}	
	public void findLocation() {
		//TO EMULATOR USERS: Must use DDMS (Window>Perspective) to set a GPS location before clicking organise
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(location != null) {p.giveLocation(location);}
		else{locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);}
	}
}

class MyLocationListener implements LocationListener{
	Pending pending;
	MyLocationListener(Pending pending) {
		this.pending = pending; 
	}
	public void onLocationChanged(Location location) {makeUseOfNewLocation(location);}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
	
	//This method should find the current town from the latitude/longitude of the location
	public void makeUseOfNewLocation(Location location) {
		pending.giveLocation(location);
	}
}

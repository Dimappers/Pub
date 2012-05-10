package dimappers.android.pub;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import dimappers.android.PubData.Constants;

public class DataRequestReverseGeocoder implements IDataRequest<String, XmlableDoubleArray> {

	Context context;
	IPubService service;
	String loc;
	
	public DataRequestReverseGeocoder(Context context, String loc) {
		this.context = context;
		this.loc = loc;
	}
	
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	
	public void performRequest(IRequestListener<XmlableDoubleArray> listener, HashMap<String, XmlableDoubleArray> storedData) {
		if(storedData.containsKey(loc)) 
		{
			listener.onRequestComplete(storedData.get(loc));
		}
		else
		{
			Geocoder geocoder = new Geocoder(context);
			try {
				List<Address> addresses = geocoder.getFromLocationName(loc, 5);
				double lat = 0; 
				double latsum = 0;
				double lng = 0;
				double lngsum = 0;
				if(addresses!=null) {
					for(int i=0; i<addresses.size(); i++) {
						Address a = addresses.get(i);
						if(a!=null) 
						{
							if(lat==0) {lat = a.getLatitude();}
							else {
								latsum+=a.getLatitude();
								lat=latsum/i;
							}
							if(lng==0) {lng = a.getLongitude();}
							else {
								lngsum+=a.getLongitude();
								lng=lngsum/i;
							}
						}
					}
				}
				if(lat!=0||lng!=0)
				{
					double[] location = new double[2];
					location[0] = lat;
					location[1] = lng;
					XmlableDoubleArray locationArray = new XmlableDoubleArray(location);
					listener.onRequestComplete(locationArray);
					storedData.put(loc, locationArray);
				}
				else {listener.onRequestFail(new Exception("No addresses found"));}
			} 
			catch (IOException e) 
			{
				Log.d(Constants.MsgError,"Error in finding latitude & longitude from given location.");
				e.printStackTrace();
				listener.onRequestFail(e);
			}
		}
	}

	
	public String getStoredDataId() {
		return "Location";
	}

}
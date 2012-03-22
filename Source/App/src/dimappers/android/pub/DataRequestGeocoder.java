package dimappers.android.pub;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

public class DataRequestGeocoder implements IDataRequest<long[], XmlableString> {

	IPubService service;
	Context applicationContext;
	double latitude;
	double longitude;
	
	DataRequestGeocoder(double latitude, double longitude, Context appContext)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		applicationContext = appContext;
	}
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<XmlableString> listener,HashMap<long[], XmlableString> storedData) {
		
		long[] key = new long[2];
		key[0] = get2DP(latitude);
		key[1] = get2DP(longitude);
		
		if(storedData.containsKey(key)&&!storedData.get(key).outOfDate())
		{
			listener.onRequestComplete(storedData.get(key));
		}
		else
		{
			Geocoder gc = new Geocoder(applicationContext);
			XmlableString place = null; 
			try {
				List<Address> list = gc.getFromLocation(latitude, longitude, 5);
				int i = 0;
				while (i<list.size()) 
				{
					String temp = list.get(i).getLocality();
					if(temp!=null) {place = new XmlableString(temp); break;}
					i++;
				}
				if(place!=null)
				{
					storedData.put(key, place);
				}
				listener.onRequestComplete(place);
			}
			//This is thrown if the phone has no Internet connection.
			catch (IOException e) {
				listener.onRequestFail(e);
			}
		}
	}

	public String getStoredDataId() {
		return "Place";
	}
	
	private long get2DP(double value) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(value)).longValue();
	}

}

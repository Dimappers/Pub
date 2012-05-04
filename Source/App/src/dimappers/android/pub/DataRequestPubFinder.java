package dimappers.android.pub;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import dimappers.android.PubData.Constants;

public class DataRequestPubFinder implements IDataRequest<Integer, PlacesList> {

	 IPubService service;
	 private double latitude;
	 private double longitude;
	 private String keyword;
	 
	 private static final HttpTransport transport = new ApacheHttpTransport();
	 
	 private final static int radiusForSearch = 1000;
	
	 DataRequestPubFinder(double latitude, double longitude, String keyword) {
		 this.latitude = latitude;
		 this.longitude = longitude;
		 this.keyword = keyword;
	 }
	 
	 DataRequestPubFinder(double latitude, double longitude) {
		 this.latitude = latitude;
		 this.longitude = longitude;
		 this.keyword = "";
	 }
	 
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}

	public void performRequest(IRequestListener<PlacesList> listener,HashMap<Integer, PlacesList> storedData) {
		if(storedData.containsKey(DataRequestPubFinder.getKey(longitude, latitude, keyword)) && 
				!storedData.get(DataRequestPubFinder.getKey(longitude, latitude, keyword)).isOutOfDate()) //if we already have some pubs and they are not out of date
		{
			Log.d(Constants.MsgInfo, "Already have pubs.");
			listener.onRequestComplete(storedData.get(DataRequestPubFinder.getKey(longitude, latitude, keyword)));
		}
		else{
			Log.d(Constants.MsgInfo, "Getting pubs from Google.");
			try {
				HttpRequestFactory httpRequestFactory = transport.createRequestFactory(new HttpRequestInitializer() {
					  public void initialize(HttpRequest request) {
						  GoogleHeaders headers = new GoogleHeaders();
						  headers.setApplicationName("Pub");
						  request.setHeaders(headers);
						  JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
						  request.addParser(parser);
					  }
					  });
				HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(Constants.PLACES_SEARCH_URL));
				request.getUrl().put("key", Constants.API_KEY);
				request.getUrl().put("sensor", "true");	
			    
			    request.getUrl().put("location", latitude + "," + longitude);
			    request.getUrl().put("types", "bar");
			    
			    if(keyword.length()==0)
			    {
			    	request.getUrl().put("radius", radiusForSearch);
			    }
			    else
			    {
			    	request.getUrl().put("radius", radiusForSearch*2);
			    	request.getUrl().put("keyword",keyword);
			    }
			     
			    PlacesList places = request.execute().parseAs(PlacesList.class);	
			    
			    if(places.status.equals("OK")) 
			    {
			    	Calendar current = Calendar.getInstance();
			    	current.add(Calendar.DATE, Constants.PubOutOfDateTime);
			    	places.setOutOfDate(current);
			    	storedData.put(DataRequestPubFinder.getKey(longitude, latitude, keyword), places);
			    	listener.onRequestComplete(places);
			    }
			    else if(places.status.equals("ZERO_RESULTS"))
			    {
			    	listener.onRequestComplete(places);
			    }
			    else
			    {
			    	listener.onRequestFail(new Exception(places.status));	
			    }
			}
		   catch (Exception e) {
			    listener.onRequestFail(e);
		   }
		}
	}

	public String getStoredDataId() {
		return "PubLists";
	}
	
	private static double get2DP(double value) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(value));
	}
	public static Integer getKey(double latitude, double longitude, String keyword) {
		int lat = (int)(1000*latitude);
		int lng = (int)(1000*longitude);
		int keyhash = keyword.hashCode();
		return lat + lng + keyhash;
	}

}

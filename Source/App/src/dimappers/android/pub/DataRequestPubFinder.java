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
		if(storedData.containsKey(getKey()))
		{
			Log.d(Constants.MsgInfo, "Already have pubs.");
			listener.onRequestComplete(storedData.get(getKey()));
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
			    	storedData.put(getKey(), places);
			    	listener.onRequestComplete(places);
			    }
			    //TODO: deal with no results separately because this isn't an error
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
	
	private double get2DP(double value) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(value));
	}
	private Integer getKey() {
		return new Double(Math.pow(2.0, get2DP(latitude))*Math.pow(3.0, get2DP(longitude))*Math.pow(5.0, keyword.hashCode())).hashCode();
	}

}

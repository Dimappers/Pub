package dimappers.android.pub;

import java.io.IOException;
import java.util.List;

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

import dimappers.android.pub.PlacesAutocompleteList.PlaceAutoComplete;

public class PubFinder {

 private String API_KEY = "AIzaSyBg0eJlYa_70fG8dc1xdHKFT3BoEWwEQ6M";

 private final String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
 private final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
 private static  String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
 
 private static final HttpTransport transport = new ApacheHttpTransport();
 
 private double latitude;
 private double longitude;
 
 private int radiusForSearch = 1000;
 
 public PubFinder(double latitude, double longitude) {
	 this.latitude = latitude;
	 this.longitude = longitude;
 }
 
 public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
	 return transport.createRequestFactory(new HttpRequestInitializer() {
		  public void initialize(HttpRequest request) {
			  GoogleHeaders headers = new GoogleHeaders();
			  headers.setApplicationName("Pub");
			  request.setHeaders(headers);
			  JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
			  request.addParser(parser);
		  }
		  });
 }
  
  public HttpRequest setUp(String URL) throws IOException {
	  HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
	  HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(URL));
	  request.getUrl().put("key", API_KEY);
	  request.getUrl().put("sensor", "true");
	  return request;
  }
 
  public List<Place> performSearch() throws Exception {return performSearch("");}
  
  public List<Place> performSearch(String keyword) throws Exception {
	   try {
		    HttpRequest request = setUp(PLACES_SEARCH_URL);	
		    
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
		    return places.results;
	   }
	   catch (HttpResponseException e) {
		    System.err.println(e.getResponse().parseAsString());
		    throw e;
	   }
  }
  
  public PlaceDetail performDetails(String reference) throws Exception {
	  try {
		   HttpRequest request = setUp(PLACES_DETAILS_URL);
		   
		   request.getUrl().put("reference", reference);  
		   return request.execute().parseAs(PlaceDetail.class);
	  } 
	  catch (HttpResponseException e) {
		   System.err.println(e.getResponse().parseAsString());
		   throw e;
	  }
	 }
   
  public List<PlaceAutoComplete> performAutoComplete(String keyword) throws Exception {
   try {
	    HttpRequest request = setUp(PLACES_AUTOCOMPLETE_URL);
	    
	    request.getUrl().put("input", keyword);
	    request.getUrl().put("location", latitude + "," + longitude);
	    request.getUrl().put("radius", radiusForSearch);
	    PlacesAutocompleteList places = request.execute().parseAs(PlacesAutocompleteList.class);
	    return places.predictions;
   } 
   catch (HttpResponseException e)
   {
	    System.err.println(e.getResponse().parseAsString());
	    throw e;
   }
  }
}
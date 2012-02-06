package dimappers.android.pub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.JsonEncoding;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson.JacksonFactory;

import dimappers.android.PubData.Constants;

import android.util.Base64;
import android.util.Log;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PubFinder {

 private String API_KEY = "AIzaSyBg0eJlYa_70fG8dc1xdHKFT3BoEWwEQ6M";

 private final String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
 
 private double latitude;
 private double longitude;
 
 public PubFinder(double latitude, double longitude) {
	 this.latitude = latitude;
	 this.longitude = longitude;
 }
  
  public List<Place> performSearch() throws Exception {
	   try {
		    HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
		    HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
		    request.getUrl().put("key", API_KEY);
		    request.getUrl().put("location", latitude + "," + longitude);
		    request.getUrl().put("radius", 5000);
		    request.getUrl().put("sensor", "true");
		    request.getUrl().put("types", "bar");
		     
		    PlacesList places = request.execute().parseAs(PlacesList.class);
		    
		    return places.results;
	   }
	   catch (HttpResponseException e) {
		    System.err.println(e.getResponse().parseAsString());
		    throw e;
	   }
  }
  
  private static final HttpTransport transport = new ApacheHttpTransport();
  
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
  
  /*public void performDetails(String reference) throws Exception {
	  try {
		   HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
		   HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
		   request.getUrl().put("key", API_KEY);
		   request.getUrl().put("reference", reference);
		   request.getUrl().put("sensor", "false");
		    
		   PlaceDetail place = request.execute().parseAs(PlaceDetail.class);
		 
	  } 
	  catch (HttpResponseException e) {
		   System.err.println(e.getResponse().parseAsString());
		   throw e;
	  }
	 }
  
  private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
  
  
  public void performAutoComplete() throws Exception {
   try {
	    HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
	    HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_AUTOCOMPLETE_URL));
	    request.getUrl().put("key", API_KEY);
	    request.getUrl().put("input", "mos");
	    request.getUrl().put("location", latitude + "," + longitude);
	    request.getUrl().put("radius", 500);
	    request.getUrl().put("sensor", "false");
	    PlacesAutocompleteList places = request.execute().parseAs(PlacesAutocompleteList.class);
   } 
   catch (HttpResponseException e)
   {
	    System.err.println(e.getResponse().parseAsString());
	    throw e;
   }
  }*/
}
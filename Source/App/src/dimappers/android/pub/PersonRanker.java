package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

public class PersonRanker {
	
	final int maxDist = 2;
	
	IPubService service;
	HistoryStore historyStore;
	User[] friends;
	User[] allFriends;
	Facebook facebook;
	Location currentLocation;
	List<PubEvent> trips;
	
	PubEvent currentEvent;
	
	PersonRanker(PubEvent currentEvent, IPubService service, User[] facebookFriends)
	{
		//TODO: get required things from service: 
			 historyStore = new HistoryStore(); 
			 currentLocation = new Location("location");
		   
		this.service = service;
		if (!Constants.emulator)
		{
			this.facebook = service.GetFacebook();
			
			friends = facebookFriends;//(all facebook friends)    
			    
			this.currentEvent = currentEvent;
			trips = historyStore.getPubTrips();
			removeTooFarAwayFriends();
			
			JSONObject myPhotos = null;
			JSONObject myPosts = null;
			try {
				myPhotos = new JSONObject(facebook.request("me/photos"));
				myPosts = new JSONObject(facebook.request("me/feed"));
			}
			catch(Exception e) {Log.d(Constants.MsgError, "Exception thrown while retrieving Facebook photos & posts.");}
			
			User friend;
			for(int i = 0; i<friends.length; i++)
			{
				friend = friends[i];
				friend.setRank(findFacebookClosenessRank(friend, myPhotos, myPosts) + findPreviousPubTrips(friend));
			}
			if(friends.length>0) {
				friends = MergeSort(friends);
				allFriends = new User[friends.length];
				for(int i = 0; i < friends.length; i++)
				{
					allFriends[i] = friends[i];
				}
				int n = Math.min(historyStore.getAverageNumberOfFriends(), friends.length);
				currentEvent.emptyGuestList();
				for(int i = 0; i<n; i++)
				{
					currentEvent.AddUser(friends[i]);
				}
			}
		}
	}

	public User[] getArrayOfRankedFriends() {return allFriends;}
	
	public PubEvent getEvent() {return currentEvent;}
	
	private User[] MergeSort(User[] list)
	{
		if (list.length<=1) return list;
		User[] lista = new User[(int) list.length/2];
		User[] listb = new User[list.length-lista.length];
		for(int i = 0; i<list.length; i++)
		{
			if(i<lista.length){lista[i] = list[i];}
			else {listb[i-lista.length] = list[i];}
		}
		lista = MergeSort(lista);
		listb = MergeSort(listb);
		return Merge(lista, listb);
	}
	private User[] Merge(User[] lista, User[] listb)
	{
		int a = 0;
		int b = 0;
		int c = 0;
		User[] temp = new User[lista.length + listb.length];
		while(c!=temp.length)
		{
			if(lista.length==a) {temp[c] = listb[b]; b++;}
			else if(listb.length==b) {temp[c] = lista[a]; a++;}
			else
			{
				if(lista[a].getRank()>listb[b].getRank()) {temp[c] = lista[a]; a++;}
				else {temp[c] = listb[b]; b++;}
			}
			c++;
		}
		return temp;
	}

	private int findPreviousPubTrips(User friend) {
		int rank = 0;

		for(PubEvent trip : trips)
		{
			rank += isInGuestList(trip,friend);
		}
		return rank;
	}

	private int isInGuestList(PubEvent trip, User friend) {
		if(friend.equals(trip.GetHost())) {return 2;}
		for (User guest : trip.GetUsers())
		{
			if(friend.equals(guest)) {return 1;}
		}
		return 0;
	}

	private int findFacebookClosenessRank(User friend, JSONObject myPhotos, JSONObject myPosts) {
		return findMutuallyTaggedPhotos(friend.getUserId(), myPhotos) + findMutuallyTaggedPosts(friend.getUserId(), myPosts);
	}

	private int findMutuallyTaggedPhotos(Long userId, JSONObject myPhotos) {
		int photoNumber = 0;
		try
		{
			JSONArray photos = myPhotos.getJSONArray("data");
			for (int i = 0; i<photos.length(); i++)
			{
				JSONObject photo = (JSONObject) photos.get(i);
				if(Long.parseLong(photo.getJSONObject("from").getString("id"))==userId) {photoNumber++;}
				JSONArray tags = photo.getJSONObject("tags").getJSONArray("data");
				for (int j = 0; j<tags.length(); j++)
				{
					if(userId==Long.parseLong(((JSONObject)tags.get(j)).getString("id"))) {photoNumber++;};
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d(Constants.MsgError, "Error finding photo information.");
		}	
		return photoNumber;
	}

	private int findMutuallyTaggedPosts(Long userId, JSONObject myPosts) {
		int postNumber = 0;
		try
		{
			JSONArray posts = myPosts.getJSONArray("data");
			for (int i = 0; i<posts.length(); i++)
			{
				JSONObject post = (JSONObject) posts.get(i);
				if(Long.parseLong(post.getJSONObject("from").getString("id"))==userId) {postNumber++;}
				JSONArray to = post.getJSONObject("to").getJSONArray("data");
				for(int j = 0; j<to.length(); j++)
				{
					if(Long.parseLong(((JSONObject)to.get(j)).getString("id"))==userId) {postNumber++;}
				}
				try
				{
					JSONArray likes = post.getJSONObject("likes").getJSONArray("data");
					for(int j = 0; j<to.length(); j++)
					{
						if(Long.parseLong(((JSONObject)likes.get(j)).getString("id"))==userId) {postNumber++;}
					}
				}
				catch(JSONException e) {Log.d(Constants.MsgError, "There are no likes for this item.");}
				try
				{
					JSONArray comments = post.getJSONObject("comments").getJSONArray("data");
					for(int j = 0; j<to.length(); j++)
					{
						if(Long.parseLong(((JSONObject)comments.get(j)).getJSONObject("from").getString("id"))==userId) {postNumber++;}
					}
				}
				catch(JSONException e) {Log.d(Constants.MsgError, "There are no comments for this item.");}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d(Constants.MsgError, "Error finding post information.");
		}	
		return postNumber;
	}

	private void removeTooFarAwayFriends()
	{
		int removedFriends = 0;
		for(int i = 0; i<friends.length; i++)
		{
			if(isTooFarAway(friends[i].getLocation())) {
				friends[i]=null; 
				removedFriends++;
			}
		}
		int j = 0;
		User[] tmp = new User[friends.length-removedFriends];
		for(int i = 0; i<tmp.length; i++)
		{
			while(friends[j]==null) {j++;}
			tmp[i] = friends[j];
			j++;
		}
		friends = tmp;
	}

	private boolean isTooFarAway(double[] location) {
		if(location==null) 
		{
			return false;
		}
		double latitude = location[0];
		double longitude = location[1];
		
		double radiusOfEarth = 6371.0f; // km
		double dLat = toRad(currentLocation.getLatitude()-latitude);
		double dLng = toRad(currentLocation.getLongitude()-longitude);
		double curLng = toRad(currentLocation.getLongitude());
		double curLat = toRad(currentLocation.getLatitude());

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLng/2) * Math.sin(dLng/2) * Math.cos(curLng) * Math.cos(curLat); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = radiusOfEarth * c;
		
		if (d<maxDist) {return false;}
		return true;
	}
	private double toRad(double angle)
	{
		return (angle*Math.PI)/180.0;
	}
}

package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
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
	
	IPubService service;
	HistoryStore historyStore;
	User[] facebookFriends;
	Location currentLocation;
	List<PubEvent> trips;
	Facebook facebook;
	JSONObject myPosts = null;
	JSONObject myPhotos = null;
	boolean gotPosts = false;
	boolean gotPhotos = false;
	
	//Constants for ranking people
	private final int photoValue = 1;
	private final int photoFromValue = 0;
	
	private final int postValue = 1;
	private final int postCommentValue = 1;
	private final int postCommentTagValue = 1;
	private final int postLikeValue = 1;
	private final int postTagValue = 1;
	
	private final int guestValue = 1;
	private final int hostValue = 2;
	
	private final int maxDist = 2;
	
	PubEvent currentEvent;
	
	IRequestListener<PubEvent> listener;
	
	PersonRanker(PubEvent currentEvent, IPubService service, Location currentLocation, User[] facebookFriends, final IRequestListener<PubEvent> listener)
	{
		historyStore = service.getHistoryStore();
		this.currentLocation = currentLocation;
		this.listener = listener;

		this.service = service;
		this.facebook = service.GetFacebook();

		this.facebookFriends = facebookFriends;  

		this.currentEvent = currentEvent;
		trips = historyStore.getPubTrips();
		removeTooFarAwayFriends();

		DataRequestGetFacebookPosts posts = new DataRequestGetFacebookPosts();
		service.addDataRequest(posts, new IRequestListener<XmlJasonObject>(){

			public void onRequestComplete(XmlJasonObject data) {
				myPosts = data;
				gotPosts = true;
				if(gotPhotos) {doRanking();}
			}

			public void onRequestFail(Exception e) {
				Log.d(Constants.MsgError, "Error getting posts from Facebook: " + e.getMessage());
				//TODO: write this properly
			}
		});

		DataRequestGetPhotos photos = new DataRequestGetPhotos();
		service.addDataRequest(photos, new IRequestListener<XmlJasonObject>() {

			public void onRequestComplete(XmlJasonObject data) {
				myPhotos = data;
				gotPhotos = true;
				if(gotPosts) {doRanking();}
			}

			public void onRequestFail(Exception e) {
				Log.d(Constants.MsgError, "Error getting photos from Facebook: " + e.getMessage());
				//TODO: write this properly
			}
		});
		
	}
	
	private void doRanking() {
		Log.d(Constants.MsgInfo, "Starting at: " + Calendar.getInstance().getTime().toString());
		Log.d(Constants.MsgInfo, "Friend count: " + facebookFriends.length);
		User friend;
		for(int i = 0; i<facebookFriends.length; i++)
		{
			friend = facebookFriends[i];
			friend.setRank(findFacebookClosenessRank(friend) + findPreviousPubTrips(friend));
		}
		if(facebookFriends.length>0) {
			facebookFriends = MergeSort(facebookFriends);
			int n = Math.min(historyStore.getAverageNumberOfFriends(), facebookFriends.length);
			currentEvent.emptyGuestList();
			for(int i = 0; i<n; i++)
			{
				currentEvent.AddUser(facebookFriends[i]);
			}
		}
		Log.d(Constants.MsgInfo, "Finished at: " + Calendar.getInstance().getTime().toString());
		listener.onRequestComplete(currentEvent);
	}
	
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
		if(friend.equals(trip.GetHost())) {return hostValue;}
		for (User guest : trip.GetUsers())
		{
			if(friend.equals(guest)) {return guestValue;}
		}
		return 0;
	}

	private int findFacebookClosenessRank(User friend) {
		return findMutuallyTaggedPhotos(friend.getUserId()) + findMutuallyTaggedPosts(friend.getUserId());
	}

	private int findMutuallyTaggedPhotos(Long userId) {
		int photoNumber = 0;
		try
		{
			JSONArray photos = myPhotos.getJSONArray("data");
			for (int i = 0; i<photos.length(); i++)
			{
				JSONObject photo = photos.getJSONObject(i);
				
				/*Next line changes the rank of the person whose photo it is - we might not want to do this because it can massively skew the results!*/
				if(Long.parseLong(photo.getJSONObject("from").getString("id"))==userId) {photoNumber+=photoFromValue;}
				
				JSONArray tags = photo.getJSONObject("tags").getJSONArray("data");
				for (int j = 0; j<tags.length(); j++)
				{
					if(userId==Long.parseLong((tags.getJSONObject(j)).getString("id"))) {photoNumber+=photoValue;};
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d(Constants.MsgError, "Error finding photo information.");
		}	
		
		return photoNumber;
	}

	private int findMutuallyTaggedPosts(Long userId) {
		//This bit appears to not be working anymore - potentially to do with Facebook changes...
		int postNumber = 0;
		try
		{
			JSONArray posts = myPosts.getJSONArray("data");
			for (int i = 0; i<posts.length(); i++)
			{
				JSONObject post = (JSONObject) posts.get(i);
				
				//Person the post is from
				if(Long.parseLong(post.getJSONObject("from").getString("id"))==userId) {postNumber+=postValue;}
				
				//People tagged in the post
				if(post.has("message_tags"))
				{
					JSONObject messageTagsJObj = post.getJSONObject("message_tags"); 
					Iterator<String> it = messageTagsJObj.keys();
					while(it.hasNext())
					{
						JSONArray tags = messageTagsJObj.getJSONArray(it.next());
						for(int j = 0 ; j<tags.length(); j++)
						{
							JSONObject tag = tags.getJSONObject(j);
							if(Long.parseLong(tag.getString("id"))==userId) {postNumber+=postValue;}
						}
					}
				}
				
				//TODO: deal with story_tags
				
				//Person the post is from & all people tagged as being "with you" in the post
				if(post.has("to"))
				{
					JSONArray to = post.getJSONObject("to").getJSONArray("data");
					for(int j = 0; j<to.length(); j++)
					{
						if(Long.parseLong((to.getJSONObject(j)).getString("id"))==userId) {postNumber+=postTagValue;}
					}
				}
				
				//People who have liked the post
				if(post.has("likes"))
				{
					JSONArray likes = post.getJSONObject("likes").getJSONArray("data");
					for(int j = 0; j<likes.length(); j++)
					{
						if(Long.parseLong((likes.getJSONObject(j)).getString("id"))==userId) {postNumber+=postLikeValue;}
					}
				}
				
				//People who have commented on the post
				if(post.getJSONObject("comments").has("data"))
				{
					JSONArray comments = post.getJSONObject("comments").getJSONArray("data");
					for(int j = 0; j<comments.length(); j++)
					{
						JSONObject comment = comments.getJSONObject(j);
						if(Long.parseLong(comment.getJSONObject("from").getString("id"))==userId) {postNumber+=postCommentValue;}
						
						//People who are tagged in comments
						if(comment.has("message_tags"))
						{
							JSONArray message_tags = comment.getJSONArray("message_tags");
							for(int k = 0; k<message_tags.length(); k++)
							{
								if(Long.parseLong(message_tags.getJSONObject(k).getString("id"))==userId) {postNumber+=postCommentTagValue;}
							}
						}
					}
				}

			}
		} catch (JSONException e) {
			Log.d(Constants.MsgError, "Error finding post information.");
		}	
		return postNumber;
	}

	private void removeTooFarAwayFriends()
	{
		int removedFriends = 0;
		for(int i = 0; i<facebookFriends.length; i++)
		{
			if(isTooFarAway(facebookFriends[i].getLocation())) {
				facebookFriends[i]=null; 
				removedFriends++;
			}
		}
		int j = 0;
		User[] tmp = new User[facebookFriends.length-removedFriends];
		for(int i = 0; i<tmp.length; i++)
		{
			while(facebookFriends[j]==null) {j++;}
			tmp[i] = facebookFriends[j];
			j++;
		}
		facebookFriends = tmp;
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

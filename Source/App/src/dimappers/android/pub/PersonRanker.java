package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
	private final static int photoValue = 1;
	private final static int photoFromValue = 0;
	
	private final static int postValue = 1;
	private final static int postCommentValue = 1;
	private final static int postCommentTagValue = 1;
	private final static int postLikeValue = 1;
	private final static int postTagValue = 1;
	
	private final static int guestValue = 1;
	private final static int hostValue = 2;
	
	private final static int rankFromPhotosFromWho = -1;
	private final static int rankFromPhotosTagged = -2;
	
	private final static int rankFromPostsFromWho = 0;
	private final static int rankFromPostsLiking = -3;
	private final static int rankFromPostsWithYou = -4;
	private final static int rankFromPostsTagged = -5;
	private final static int rankFromPostsComment = -6;
	private final static int rankFromPostsTaggedInComment = -7;

	private final static int rankFromHistory = 1;
	
	private final int maxDist = 2;
	
	PubEvent currentEvent;
	
	IRequestListener<PubEvent> listener;
	
	PersonRanker(PubEvent currentEvent, final Pending pending, Location currentLocation, User[] facebookFriends, final IRequestListener<PubEvent> listener)
	{
		this.service = pending.service;
		historyStore = service.getHistoryStore();
		this.currentLocation = currentLocation;
		this.listener = listener;

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
				pending.errorOccurred();
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
				pending.errorOccurred();
			}
		});
		
	}
	
	private void doRanking() {
		Log.d(Constants.MsgInfo, "Starting at: " + Calendar.getInstance().getTime().toString());
		Log.d(Constants.MsgInfo, "Friend count: " + facebookFriends.length);
		
		if(facebookFriends.length>0) 
		{
			rankFromPosts();
			rankFromPhotos();
			rankFromHistory();
			
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
	
	private void rankFromPosts() {
		if(myPosts.has("data"))
		{
			try {
				JSONArray myPostsDataArray = myPosts.getJSONArray("data");
				for(int i = 0; i<myPostsDataArray.length(); i++)
				{
					JSONObject post = (JSONObject) myPostsDataArray.get(i);
					
					//Person the post is from
					addToRankOf(Long.parseLong(post.getJSONObject("from").getString("id")), postValue, rankFromPostsFromWho);
					
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
								addToRankOf(Long.parseLong(post.getJSONObject("from").getString("id")), postTagValue, rankFromPostsTagged);
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
							addToRankOf(Long.parseLong(post.getJSONObject("from").getString("id")), postTagValue, rankFromPostsWithYou);
						}
					}
					
					//People who have liked the post
					if(post.has("likes"))
					{
						JSONArray likes = post.getJSONObject("likes").getJSONArray("data");
						for(int j = 0; j<likes.length(); j++)
						{
							addToRankOf(Long.parseLong(post.getJSONObject("from").getString("id")), postLikeValue, rankFromPostsLiking);
						}
					}
					
					//People who have commented on the post
					if(post.getJSONObject("comments").has("data"))
					{
						JSONArray comments = post.getJSONObject("comments").getJSONArray("data");
						for(int j = 0; j<comments.length(); j++)
						{
							JSONObject comment = comments.getJSONObject(j);
							addToRankOf(Long.parseLong(post.getJSONObject("from").getString("id")), postCommentValue, rankFromPostsComment);
							
							//People who are tagged in comments
							if(comment.has("message_tags"))
							{
								JSONArray message_tags = comment.getJSONArray("message_tags");
								for(int k = 0; k<message_tags.length(); k++)
								{
									addToRankOf(Long.parseLong(post.getJSONObject("from").getString("id")), postCommentTagValue, rankFromPostsComment);
								}
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.d(Constants.MsgError, "Error finding post information.");
			}
		}
		else {Log.d(Constants.MsgError, "No data array available in myPosts.");}
	}
	
	private void rankFromPhotos() {
		if(myPhotos.has("data"))
		{
			try {
				JSONArray myPhotosDataArray = myPhotos.getJSONArray("data");
				
				for (int i = 0; i<myPhotosDataArray.length(); i++)
				{
					JSONObject photo = myPhotosDataArray.getJSONObject(i);
					
					/*Next line changes the rank of the person whose photo it is - we might not want to do this because it can massively skew the results!*/
					addToRankOf(Long.parseLong(photo.getJSONObject("from").getString("id")), photoFromValue, rankFromPhotosFromWho);
					
					JSONArray tags = photo.getJSONObject("tags").getJSONArray("data");
					for (int j = 0; j<tags.length(); j++)
					{
						addToRankOf(Long.parseLong(photo.getJSONObject("from").getString("id")), photoValue, rankFromPhotosTagged);
					}
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				Log.d(Constants.MsgError, "Error finding photos information.");
			}
		}
	}
	
	private void addToRankOf(long facebookId, int amount, int fromWhere) {
		for(User person: facebookFriends)
		{
			if(person.getUserId()==facebookId)
			{
				person.setRank(
						person.getRank()+amount);
				if(Constants.debug)
				{
					switch(fromWhere)
					{
					case rankFromPhotosFromWho : {person.PhotosFromWho+=amount; break;}
					case rankFromPhotosTagged : {person.PhotosTagged+=amount; break;}
					case rankFromPostsComment : {person.PostsComments+=amount; break;}
					case rankFromPostsFromWho : {person.PostsFromWho+=amount; break;}
					case rankFromPostsLiking : {person.PostsLiked+=amount; break;}
					case rankFromPostsTagged : {person.PostsTagged+=amount; break;}
					case rankFromPostsTaggedInComment : {person.PostsTaggedInComment+=amount; break;}
					case rankFromPostsWithYou : {person.PostsWithYou+=amount; break;}
					}
				}
				return;
			}
		}
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
	
	private void rankFromHistory() {
		for(PubEvent trip : trips)
		{
			for(User friend : facebookFriends)
			{
				friend.setRank(friend.getRank()+isInGuestList(trip,friend));
				friend.History+=isInGuestList(trip,friend);
			}
		}
	}

	private int isInGuestList(PubEvent trip, User friend) {
		if(friend.equals(trip.GetHost())) {return hostValue;}
		for (User guest : trip.GetUsers())
		{
			if(friend.equals(guest)) {return guestValue;}
		}
		return 0;
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

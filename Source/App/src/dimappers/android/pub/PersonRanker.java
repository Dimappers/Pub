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

import android.content.ContentResolver;
import android.database.Cursor;
import android.location.Location;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

public class PersonRanker {

	IPubService service;
	HistoryStore historyStore;
	long me;
	private AppUser[] facebookFriends;
	AppUser[] removedFriendsList;
	Location currentLocation;
	List<PubEvent> trips;
	Facebook facebook;
	JSONObject myPosts = null;
	JSONObject myPhotos = null;
	boolean gotPosts = false;
	boolean gotPhotos = false;
	boolean gotLocations = true; //true once all of the facebook friends have attempted to be geocoded
	int gotLocationsOfSomeForm = 0; //stores the number of friends who have either been geocoded or don't have a location to geocode
	
	// Constants for ranking people
	private final static int photoValue = 1;
	private final static int photoFromValue = 0;
	private final static int photoLikeValue = 1;
	private final static int photoCommentValue = 1;

	private final static int postValue = 1;
	private final static int postCommentValue = 1;
	private final static int postCommentTagValue = 1;
	private final static int postLikeValue = 1;
	private final static int postTagValue = 1;

	private final static int guestValue = 1;
	private final static int hostValue = 2;

	private final static int rankFromPhotosFromWho = -1;
	private final static int rankFromPhotosTagged = -2;
	private final static int rankFromPhotosLiked = 3;
	private final static int rankFromPhotosCommented = 4;

	private final static int rankFromPostsFromWho = 0;
	private final static int rankFromPostsLiking = -3;
	private final static int rankFromPostsWithYou = -4;
	private final static int rankFromPostsTagged = -5;
	private final static int rankFromPostsComment = -6;
	private final static int rankFromPostsTaggedInComment = -7;

	private final int maxDist = 2;

	PubEvent currentEvent;

	IRequestListener<PubEvent> listener;

	ContentResolver contentResolver;

	PersonRanker(PubEvent currentEvent, final Pending pending,
			Location currentLocation, AppUser[] facebookFriends,
			final IRequestListener<PubEvent> listener,
			ContentResolver contentResolver) {
		this.service = pending.service;
		historyStore = service.getHistoryStore();
		this.currentLocation = currentLocation;
		this.listener = listener;
		this.contentResolver = contentResolver;

		this.facebook = service.GetFacebook();

		this.setFacebookFriends(facebookFriends);

		me = service.GetActiveUser().getUserId();

		this.currentEvent = currentEvent;
		trips = historyStore.getPubTrips();
		removeTooFarAwayFriends();

		DataRequestGetFacebookPosts posts = new DataRequestGetFacebookPosts();
		service.addDataRequest(posts, new IRequestListener<XmlJasonObject>() {

			public void onRequestComplete(XmlJasonObject data) {
				myPosts = data;
				gotPosts = true;
				if (gotPhotos && gotLocations) {
					doRanking();
				}
			}

			public void onRequestFail(Exception e) {
				Log.d(Constants.MsgError, "Error getting posts from Facebook: "
						+ e.getMessage());
				pending.errorOccurred();
			}
		});

		DataRequestGetPhotos photos = new DataRequestGetPhotos();
		service.addDataRequest(photos, new IRequestListener<XmlJasonObject>() {

			public void onRequestComplete(XmlJasonObject data) {
				myPhotos = data;
				gotPhotos = true;
				if (gotPosts && gotLocations) {
					doRanking();
				}
			}

			public void onRequestFail(Exception e) {
				Log.d(Constants.MsgError,
						"Error getting photos from Facebook: " + e.getMessage());
				pending.errorOccurred();
			}
		});
		
		/*for(final AppUser friend : getFacebookFriends())
		{
			if(friend.getLocationName() != null)
			{
				DataRequestReverseGeocoder geocodeName = new DataRequestReverseGeocoder(pending, friend.getLocationName());
				
				service.addDataRequest(geocodeName, new IRequestListener<XmlableDoubleArray>()
				{
					public void onRequestComplete(XmlableDoubleArray data) {
						double[] loc = data.array;
						if(loc.length==2)
						{
							friend.setLocation(loc);
							++gotLocationsOfSomeForm;
							
							if(gotLocationsOfSomeForm == getFacebookFriends().length)
							{
								gotLocations = true;
							}
							
							if(gotPhotos && gotPosts && gotLocations)
							{
								doRanking();
							}
						}						
					}

					//@Override
					public void onRequestFail(Exception e) {
						if(e instanceof IOException)
						{
							Log.d(Constants.MsgError, "Error reverse geocoding peoples locations");
							pending.errorOccurred();
						}
						
						++gotLocationsOfSomeForm;
						
						if(gotLocationsOfSomeForm == getFacebookFriends().length)
						{
							gotLocations = true;
						}
						
						if(gotPhotos && gotPosts && gotLocations)
						{
							doRanking();
						}
					}
			
				});
			}
			else
			{
				++gotLocationsOfSomeForm; //we can't get a location
			}
		}*/

	}

	
	AppUser[] getFacebookFriends() {
		return facebookFriends;
	}


	private void setFacebookFriends(AppUser[] facebookFriends) {
		this.facebookFriends = facebookFriends;
	}


	private void doRanking() {
		Log.d(Constants.MsgInfo, "Starting at: "
				+ Calendar.getInstance().getTime().toString());
		Log.d(Constants.MsgInfo, "Friend count: " + getFacebookFriends().length);

		if (getFacebookFriends().length > 0) {
			for(AppUser friend : getFacebookFriends())
			{
				if(friend.getRank()!=0) {friend.setRank(0);}
			}
			rankFromPosts();
			rankFromPhotos();
			rankFromHistory();
			rankFromCallHistory();

			setFacebookFriends(MergeSort(getFacebookFriends()));
			AppUser[] allFriends = new AppUser[getFacebookFriends().length + removedFriendsList.length];
			for(int i = 0; i<getFacebookFriends().length; i++)
			{
				allFriends[i] = getFacebookFriends()[i];
			}
			for(int i = 0; i<removedFriendsList.length; i++)
			{
				allFriends[i+getFacebookFriends().length] = removedFriendsList[i];
			}
			setFacebookFriends(allFriends);
			
			DataRequestGetFriends.UpdateOrdering(getFacebookFriends(), service);

			int n = Math.min(historyStore.getAverageNumberOfFriends(),
					getFacebookFriends().length);
			currentEvent.emptyGuestList();
			for (int i = 0; i < n; i++) {
				currentEvent.AddUser(getFacebookFriends()[i]);
			}
		}

		Log.d(Constants.MsgInfo, "Finished at: "
				+ Calendar.getInstance().getTime().toString());
		listener.onRequestComplete(currentEvent);
	}

	private void rankFromCallHistory() {
		/*String[] strFields = { //android.provider.CallLog.Calls.NUMBER,
				//android.provider.CallLog.Calls.TYPE,
				android.provider.CallLog.Calls.CACHED_NAME,
				//android.provider.CallLog.Calls.CACHED_NUMBER_TYPE 
				};
		Cursor callCursor = contentResolver.query(
				android.provider.CallLog.Calls.CONTENT_URI, strFields, null,
				null, android.provider.CallLog.Calls.DATE + " DESC");
		if (callCursor.moveToFirst()) {
			do {
				final String name = callCursor
						.getString(callCursor
								.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
				for (final User friend : facebookFriends) {
					if(friend instanceof AppUser)
					{
						AppUser data = (AppUser) friend;
						if (data.toString().equals(name)) {
							data.setRank(data.getRank() + callLogValue);
							if (Constants.debug) {
								data.CallLogTotal++;
							}
						}
					}
				}
			} while (callCursor.moveToNext());
		}
		callCursor.close();*/
		
		String[] pplFields = { android.provider.ContactsContract.Contacts.DISPLAY_NAME, android.provider.ContactsContract.Contacts.TIMES_CONTACTED};
		
		Cursor peopleCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, pplFields, null, null, ContactsContract.Contacts.DISPLAY_NAME + " DESC");
		if (peopleCursor.moveToFirst()) {
			do {
				final String name = peopleCursor
						.getString(peopleCursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				for (User friend : getFacebookFriends()) {
					if(friend instanceof AppUser)
					{
						AppUser data = (AppUser) friend;
						if (data.toString().equals(name)) {
							friend.setRank(friend.getRank() + peopleCursor.getInt(peopleCursor
									.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED)));
							if (Constants.debug) {
								data.CallLogTotal++;
							}
						}
					}
				}
			} while (peopleCursor.moveToNext());
		}
		peopleCursor.close();
		
		/*String[] pplFields = { android.provider.ContactsContract.Contacts.DISPLAY_NAME, android.provider.ContactsContract.Contacts.TIMES_CONTACTED};
		for(User friend : facebookFriends)
		{
			if (friend instanceof AppUser)
			{
				AppUser appFriend = (AppUser) friend;
				if(!appFriend.toString().contains("'"))
				{
					Cursor personCursor = contentResolver.query(
					
							ContactsContract.Contacts.CONTENT_URI, 
							pplFields, 
							ContactsContract.Contacts.DISPLAY_NAME + "='" + appFriend.toString() + "'", 
							null, 
							ContactsContract.Contacts.DISPLAY_NAME + " DESC"
							);
					if(personCursor.moveToFirst())
					{
						appFriend.setRank(appFriend.getRank() + personCursor.getInt(personCursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED)));
						if (Constants.debug) {
							appFriend.CallLogTotal+=personCursor.getInt(personCursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
						}
					}
					personCursor.close();
				}
			}
		}*/
	}

	private void rankFromPosts() {
		if (myPosts.has("data")) {
			try {
				JSONArray myPostsDataArray = myPosts.getJSONArray("data");
				for (int i = 0; i < myPostsDataArray.length(); i++) {
					JSONObject post = (JSONObject) myPostsDataArray.get(i);

					// Person the post is from
					long from = Long.parseLong(post.getJSONObject("from")
							.getString("id"));
					addToRankOf(from, postValue, rankFromPostsFromWho);

					// People tagged in the post
					if (post.has("message_tags")) {
						JSONObject messageTagsJObj = post
								.getJSONObject("message_tags");
						Iterator<String> it = messageTagsJObj.keys();
						while (it.hasNext()) {
							JSONArray tags = messageTagsJObj.getJSONArray(it
									.next());
							for (int j = 0; j < tags.length(); j++) {
								JSONObject tag = tags.getJSONObject(j);
								addToRankOf(
										Long.parseLong(tag.getString("id")),
										postTagValue, rankFromPostsTagged);
							}
						}
					}

					// Things you've done
					if (post.has("story_tags")) {
						JSONObject storytags = post.getJSONObject("story_tags");
						Iterator<String> keys = storytags.keys();
						while (keys.hasNext()) {
							JSONArray tag = storytags.getJSONArray(keys.next());
							for (int j = 0; j < tag.length(); j++) {
								JSONObject person = tag.getJSONObject(j);
								addToRankOf(
										Long.parseLong(person.getString("id")),
										postTagValue, rankFromPostsTagged);
							}
						}
					}

					// Person the post is from (sometimes.. I don't really
					// understand what's in this one) & all people tagged as
					// being "with you" in the post
					if (post.has("to")) {
						JSONArray to = post.getJSONObject("to").getJSONArray(
								"data");
						for (int j = 0; j < to.length(); j++) {
							addToRankOf(Long.parseLong(to.getJSONObject(j)
									.getString("id")), postTagValue,
									rankFromPostsWithYou);
						}
					}

					// People who have liked the post
					if (post.has("likes")) {
						JSONArray likes = post.getJSONObject("likes")
								.getJSONArray("data");
						for (int j = 0; j < likes.length(); j++) {
							addToRankOf(Long.parseLong(likes.getJSONObject(j)
									.getString("id")), postLikeValue,
									rankFromPostsLiking);
						}
					}

					// People who have commented on the post
					if (post.getJSONObject("comments").has("data")) {
						JSONArray comments = post.getJSONObject("comments")
								.getJSONArray("data");
						for (int j = 0; j < comments.length(); j++) {
							JSONObject comment = comments.getJSONObject(j);
							addToRankOf(Long.parseLong(comment.getJSONObject(
									"from").getString("id")), postCommentValue,
									rankFromPostsComment);

							// People who are tagged in comments
							if (comment.has("message_tags")) {
								JSONArray message_tags = comment
										.getJSONArray("message_tags");
								for (int k = 0; k < message_tags.length(); k++) {
									addToRankOf(Long.parseLong(message_tags
											.getJSONObject(k).getString("id")),
											postCommentTagValue,
											rankFromPostsTaggedInComment);
								}
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.d(Constants.MsgError, "Error finding post information.");
			}
		} else {
			Log.d(Constants.MsgError, "No data array available in myPosts.");
		}
	}

	private void rankFromPhotos() {
		if (myPhotos.has("data")) {
			try {
				JSONArray myPhotosDataArray = myPhotos.getJSONArray("data");

				for (int i = 0; i < myPhotosDataArray.length(); i++) {
					JSONObject photo = myPhotosDataArray.getJSONObject(i);

					/*
					 * Next line changes the rank of the person whose photo it
					 * is - we might not want to do this because it can
					 * massively skew the results!
					 */
					addToRankOf(Long.parseLong(photo.getJSONObject("from")
							.getString("id")), photoFromValue,
							rankFromPhotosFromWho);

					JSONArray tags = photo.getJSONObject("tags").getJSONArray(
							"data");
					for (int j = 0; j < tags.length(); j++) {
						addToRankOf(
								Long.parseLong(tags.getJSONObject(j).getString(
										"id")), photoValue,
								rankFromPhotosTagged);
					}

					// Likes
					if (photo.has("likes")
							&& photo.getJSONObject("likes").has("data")) {
						JSONArray likes = photo.getJSONObject("likes")
								.getJSONArray("data");
						for (int j = 0; j < likes.length(); j++) {
							addToRankOf(Long.parseLong(likes.getJSONObject(j)
									.getString("id")), photoLikeValue,
									rankFromPhotosLiked);
						}
					}

					// Comments
					if (photo.has("comments")
							&& photo.getJSONObject("comments").has("data")) {
						JSONArray comments = photo.getJSONObject("comments")
								.getJSONArray("data");
						for (int k = 0; k < comments.length(); k++) {
							addToRankOf(
									Long.parseLong(comments.getJSONObject(k)
											.getJSONObject("from")
											.getString("id")),
									photoCommentValue, rankFromPhotosCommented);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.d(Constants.MsgError, "Error finding photos information.");
			}
		}
	}

	private void addToRankOf(long facebookId, int amount, int fromWhere) {
		if (facebookId != me) {
			for (User person : getFacebookFriends()) {
				if (person!=null && person.getUserId() == facebookId) {
					person.setRank(person.getRank() + amount);
					if (Constants.debug) {
						switch (fromWhere) {
						case rankFromPhotosFromWho: {
							person.PhotosFromWho++;
							break;
						}
						case rankFromPhotosTagged: {
							person.PhotosTagged++;
							break;
						}
						case rankFromPhotosLiked: {
							person.PhotosLiked++;
							break;
						}
						case rankFromPhotosCommented: {
							person.PhotosComments++;
							break;
						}
						case rankFromPostsComment: {
							person.PostsComments++;
							break;
						}
						case rankFromPostsFromWho: {
							person.PostsFromWho++;
							break;
						}
						case rankFromPostsLiking: {
							person.PostsLiked++;
							break;
						}
						case rankFromPostsTagged: {
							person.PostsTagged++;
							break;
						}
						case rankFromPostsTaggedInComment: {
							person.PostsTaggedInComment++;
							break;
						}
						case rankFromPostsWithYou: {
							person.PostsWithYou++;
							break;
						}
						}
					}

				}
			}
		}
	}

	public PubEvent getEvent() {
		return currentEvent;
	}

	private AppUser[] MergeSort(AppUser[] list) {
		if (list.length <= 1)
			return list;
		AppUser[] lista = new AppUser[(int) list.length / 2];
		AppUser[] listb = new AppUser[list.length - lista.length];
		for (int i = 0; i < list.length; i++) {
			if (i < lista.length) {
				lista[i] = list[i];
			} else {
				listb[i - lista.length] = list[i];
			}
		}
		lista = MergeSort(lista);
		listb = MergeSort(listb);
		return Merge(lista, listb);
	}

	private AppUser[] Merge(AppUser[] lista, AppUser[] listb) {
		int a = 0;
		int b = 0;
		int c = 0;
		AppUser[] temp = new AppUser[lista.length + listb.length];
		while (c != temp.length) {
			if (lista.length == a) {
				temp[c] = listb[b];
				b++;
			} else if (listb.length == b) {
				temp[c] = lista[a];
				a++;
			} else {
				if (lista[a].getRank() > listb[b].getRank()) {
					temp[c] = lista[a];
					a++;
				} else {
					temp[c] = listb[b];
					b++;
				}
			}
			c++;
		}
		return temp;
	}

	private void rankFromHistory() {
		for (PubEvent trip : trips) {
			for (AppUser friend : getFacebookFriends()) {
				friend.setRank(friend.getRank() + isInGuestList(trip, friend));
				friend.History += isInGuestList(trip, friend);
			}
		}
	}

	private int isInGuestList(PubEvent trip, AppUser friend) {
		if (friend.equals(trip.GetHost())) {
			return hostValue;
		}
		for (User guest : trip.GetUsers()) {
			if (friend.equals(guest)) {
				return guestValue;
			}
		}
		return 0;
	}

	private void removeTooFarAwayFriends() {
		int removedFriends = 0;
		removedFriendsList = new AppUser[getFacebookFriends().length];
		
		//make a list of friends too far away
		for (int i = 0; i < getFacebookFriends().length; i++) {
			if (isTooFarAway(getFacebookFriends()[i].getLocation())) {
				AppUser friend = getFacebookFriends()[i];
				removedFriendsList[removedFriends] = new AppUser(((AppUser) friend).writeXml()); //duplicate the friend
				getFacebookFriends()[i] = null;
				removedFriends++;
			}
		}

		AppUser[] tmp = new AppUser[getFacebookFriends().length - removedFriends];
		AppUser[] cleanedRemovedFriendsList = new AppUser[removedFriends];

		//Clean up the null entries in facebookFriends and shorted the removed friends list down to the correct size
		int j = 0; //stores where we are in the tmp array
		int k = 0; //stores where we are in the removedFriendsList
		for(AppUser user : getFacebookFriends())
		{
			if(user != null)
			{
				tmp[j] = user;
				++j;
			}
			else
			{
				cleanedRemovedFriendsList[k] = removedFriendsList[k];
				++k;
			}
		}
		
		setFacebookFriends(tmp);
		removedFriendsList = cleanedRemovedFriendsList;
	}

	private boolean isTooFarAway(double[] location) {
		if (location == null) {
			return false;
		}
		double latitude = location[0];
		double longitude = location[1];

		double radiusOfEarth = 6371.0f; // km
		double dLat = toRad(currentLocation.getLatitude() - latitude);
		double dLng = toRad(currentLocation.getLongitude() - longitude);
		double curLng = toRad(currentLocation.getLongitude());
		double curLat = toRad(currentLocation.getLatitude());

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLng / 2)
				* Math.sin(dLng / 2) * Math.cos(curLng) * Math.cos(curLat);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = radiusOfEarth * c;

		if (d < maxDist) {
			return false;
		}
		return true;
	}

	private double toRad(double angle) {
		return (angle * Math.PI) / 180.0;
	}
}

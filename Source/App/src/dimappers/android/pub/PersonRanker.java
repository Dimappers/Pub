package dimappers.android.pub;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;

public class PersonRanker {
	
	final int maxDist = 2;
	
	HistoryStore historyStore;
	User[] friends;
	Facebook facebook;
	Location currentLocation;
	List<PubEvent> trips;
	
	PubEvent currentEvent;
	
	PersonRanker(PubEvent currentEvent)
	{
		//TODO: get required things from service: 
			historyStore = new HistoryStore(); 
		    friends = new User[1];//(all facebook friends)
		    friends[0]=(new AppUser((long) 10));
		    facebook = new Facebook("0");
		    currentLocation = new Location("location");
		    
		this.currentEvent = currentEvent;
		trips = historyStore.getPubTrips();
		removeTooFarAwayFriends();
		User friend;
		for(int i = 0; i<friends.length; i++)
		{
			friend = friends[i];
			friend.setRank(findFacebookClosenessRank(friend) + findPreviousPubTrips(friend));
		}
		if(friends.length>0) {
			friends = MergeSort(friends);
			int n = Math.min(historyStore.getAverageNumberOfFriends(), friends.length);
			currentEvent.GetUsers().clear();
			for(int i = 0; i<n; i++)
			{
				currentEvent.AddUser(friends[i]);
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
				if(lista[a].getRank()<listb[b].getRank()) {temp[c] = lista[a]; a++;}
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

	private int findFacebookClosenessRank(User friend) {
		int photoNumber = findMutuallyTaggedPhotos(friend.getUserId());
		int postNumber = findMutuallyTaggedPosts(friend.getUserId());
		return photoNumber + postNumber;
	}

	private int findMutuallyTaggedPosts(Long userId) {
		// TODO: Make a call to facebook that finds out this information
		return 0;
	}

	private int findMutuallyTaggedPhotos(Long userId) {
		// TODO: Make a call to facebook that finds out this information
		return 0;
	}

	private void removeTooFarAwayFriends()
	{
		int removedFriends = 0;
		for(int i = 0; i<friends.length; i++)
		{
			if(isTooFarAway(friends[i].getLocation())) {friends[i]=null; removedFriends++;}
		}
		int j = 0;
		User[] tmp = new User[friends.length-removedFriends];
		for(int i = 0; i<tmp.length; i++)
		{
			while(friends[j]==null) {j++;}
			tmp[i] = friends[j];
		}
		friends = tmp;
	}

	private boolean isTooFarAway(double[] location) {
		if(location==null) {return false;}
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
		return (angle*2.0*Math.PI)/360.0;
	}
}

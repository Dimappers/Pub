package dimappers.android.pub;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;

public class DataRequestGetFriends extends Activity implements IDataRequest<Long, AppUserArray> {

	IPubService service;
	
	private final static String storeId = "AppUsers";
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
	}
	
	public DataRequestGetFriends() 
	{
		
	}
	
	
	public void performRequest(IRequestListener<AppUserArray> listener,
			HashMap<Long, AppUserArray> storedData) {
		Facebook facebook = service.GetFacebook();
		if(storedData.size() > 0) //if we have retrieved the friends before don't bother getting again unless they are more than a week old
		{
			if(!storedData.get(0L).isOutOfDate())
			{
				Log.d(Constants.MsgInfo, "Friends cached, not need to ask facebook");
				AppUserArray friendsArray = storedData.get(0L);
				friendsArray.setArray(friendsArray.getArray());
				listener.onRequestComplete(friendsArray); //Friends last got one week ago so we are done - TODO: Test me!!
				return;
			}
		}
		Log.d(Constants.MsgInfo, "Getting friends");
		JSONObject mefriends = null;
		try {
			Bundle bundle = new Bundle();
			bundle.putString("fields", "location, name");
			mefriends = new JSONObject(facebook.request("me/friends", bundle));
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		} 
		try {
			JSONArray jasonsFriends = mefriends.getJSONArray("data");
			final AppUser[] friends = new AppUser[jasonsFriends.length()];
			for (int i=0; i < jasonsFriends.length(); i++)
			{
				final JSONObject jason = (JSONObject) jasonsFriends.get(i);
				Long id = Long.parseLong(jason.getString("id"));
				friends[i] = new AppUser(id, jason.getString("name"));
				
				/*
				//Getting location
				if(jason.has("location"))
				{
					friends[i].setLocationName(jason.getJSONObject("location").getString("name"));
					}
				}*/
			}
			AppUserArray friendsArray = new AppUserArray(friends);
			Log.d(Constants.MsgInfo, "Friends fetched from Facebook");
			
			//only update new friends
			if(storedData.size()==0)
				//if we have never got friends before, just use the retrieved list as the new list
			{
				Log.d(Constants.MsgInfo, "New list of friends");
				storedData.put(0L, friendsArray);
				listener.onRequestComplete(friendsArray);
			}
			else
				//otherwise update the list we have previously got with the new information
			{
				Log.d(Constants.MsgInfo, "Old list of friends - updating this list");
				AppUserArray oldFriends = storedData.get(0L);
				int offset = 0;
				
				//remove friends who you're no longer friends with
				for(int i = 0; i<oldFriends.getArray().length; i++)
				{
					AppUser currentOldFriend = oldFriends.getArray()[i];
					boolean found = false;
					for(int j = 0; j<friendsArray.getArray().length; j++)
					{
						if(currentOldFriend.equals(friendsArray.getArray()[j])) {found = true;}
					}
					if(!found)
					{
						oldFriends.getArray()[i]=null;
						offset -= 1;
					}
				}
				Log.d(Constants.MsgInfo, "Removed " + offset + " friends.");
				
				//store friends you're newly friends with
				ArrayList<AppUser> newFriends = new ArrayList<AppUser>();
				for(int i = 0; i<friendsArray.getArray().length; i++)
				{
					AppUser currentNewFriend = friendsArray.getArray()[i];
					boolean found = false;
					for(int j = 0; j<oldFriends.getArray().length; j++)
					{
						if(currentNewFriend.equals(oldFriends.getArray()[j])) {found = true;}
					}
					if(!found)
					{
						newFriends.add(currentNewFriend);
						offset += 1;
					}
				}
				
				//combine these lists
				AppUser[] allCurrentFriends = new AppUser[oldFriends.getArray().length + offset];
				int currentPos = 0;
				for(int i = 0; i<oldFriends.getArray().length; i++)
				{
					if(oldFriends.getArray()[i]!=null)
					{
						allCurrentFriends[currentPos] = oldFriends.getArray()[i];
						currentPos++;
					}
				}
				for(AppUser user : newFriends)
				{
					allCurrentFriends[currentPos] = user;
					currentPos++;
				}
				
				AppUserArray newListOfFriends = new AppUserArray(allCurrentFriends);
				
				if(currentPos < oldFriends.getArray().length + offset - 1)
				{
					Log.d(Constants.MsgError, "Something has gone horribly wrong :( You're going to end up with a null pointer somewhere.");
				}
				
				storedData.put(0L, newListOfFriends);
				listener.onRequestComplete(newListOfFriends);
			}
			
			return;
			
		} catch (JSONException e) {
			listener.onRequestFail(e);
			return;
		}
	}
	
	/*private AppUser[] MergeSort(AppUser[] list)
	{
		if (list.length<=1) return list;
		AppUser[] lista = new AppUser[list.length/2];
		AppUser[] listb = new AppUser[list.length-lista.length];
		for(int i = 0; i<list.length; i++)
		{
			if(i<lista.length){lista[i] = list[i];}
			else {listb[i-lista.length] = list[i];}
		}
		lista = MergeSort(lista);
		listb = MergeSort(listb);
		return Merge(lista, listb);
	}
	private AppUser[] Merge(AppUser[] lista, AppUser[] listb)
	{
		int a = 0;
		int b = 0;
		int c = 0;
		AppUser[] temp = new AppUser[lista.length + listb.length];
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
	}*/

	
	public String getStoredDataId() {
		return storeId;
	}
	
	public static void UpdateOrdering(User[] newOrdering, IPubService service)
	{
		final AppUser[] newArray = new AppUser[newOrdering.length];
		for(int i = 0; i<newOrdering.length; i++)
		{
			final User user = newOrdering[i];
			if(user instanceof AppUser)
			{
				newArray[i] = (AppUser) user;
			}
			else
			{
				IRequestListener<AppUser> listener = new AppUserListener(i, user, newArray);
				
				
				
				service.GetAppUserFromUser(user, listener);
			}
		}
		
		HashMap<Long, AppUserArray> store = service.GetGenericStore(storeId);
		//store.put(0L, new AppUserArray(newOrdering));
		store.get(0L).setArray(newArray);
	} 
	
	private static class AppUserListener implements IRequestListener<AppUser>
	{
		
		int which;
		User user;
		AppUser[] newArray;
		
		public AppUserListener(int which, User user, AppUser[] newArray)
		{
			this.which = which;
			this.user = user;
			this.newArray = newArray;
		}
		
		public void onRequestFail(Exception e) {
			e.printStackTrace();
			newArray[which] = new AppUser(user.getUserId());
		}
		
		public void onRequestComplete(AppUser data) {
			newArray[which] = data;
		}
	}

}

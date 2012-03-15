package dimappers.android.pub;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.facebook.android.Facebook;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;

public class DataRequestGetFriends implements IDataRequest<Long, AppUserArray> {

	IPubService service;
	
	public void giveConnection(IPubService connectionInterface) {
		service = connectionInterface;
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
				friendsArray.setArray(MergeSort(friendsArray.getArray()));
				listener.onRequestComplete(friendsArray); //Friends last got one week ago so we are done - TODO: Test me!!
				return;
			}
		}
		Log.d(Constants.MsgInfo, "Getting friends");
		JSONObject mefriends = null;
		try {
			mefriends = new JSONObject(facebook.request("me/friends"));
		} catch (Exception e) {
			listener.onRequestFail(e);
			return;
		} 
		try {
			JSONArray jasonsFriends = mefriends.getJSONArray("data");
			AppUser[] friends = new AppUser[jasonsFriends.length()];
			for (int i=0; i < jasonsFriends.length(); i++)
			{
				JSONObject jason = (JSONObject) jasonsFriends.get(i);
				Long id = Long.parseLong(jason.getString("id"));
				friends[i] = new AppUser(id, jason.getString("name"));
			}
			AppUserArray friendsArray = new AppUserArray(friends);
			storedData.put(0L, friendsArray);
			Log.d(Constants.MsgInfo, "Friends fetched from Facebook");
			listener.onRequestComplete(friendsArray);
			return;
		} catch (JSONException e) {
			listener.onRequestFail(e);
			return;
		}
	}
	
	private AppUser[] MergeSort(AppUser[] list)
	{
		if (list.length<=1) return list;
		AppUser[] lista = new AppUser[(int) list.length/2];
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
	}

	public String getStoredDataId() {
		return "AppUsers";
	}

}

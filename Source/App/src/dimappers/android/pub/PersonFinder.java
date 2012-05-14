package dimappers.android.pub;

import android.content.Context;

public class PersonFinder {
	IPubService service;
	
	PersonFinder(IPubService service)
	{
		this.service = service;
	}
	
	public void getFriends(final IRequestListener<AppUserArray> listener)
	{	
		DataRequestGetFriends friends = new DataRequestGetFriends();
		service.addDataRequest(friends, listener);
	}
}

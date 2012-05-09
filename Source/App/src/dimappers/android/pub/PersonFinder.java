package dimappers.android.pub;

import android.content.Context;

public class PersonFinder {
	IPubService service;
	Context context;
	
	PersonFinder(IPubService service, Context context)
	{
		this.service = service;
		this.context = context;
	}
	
	public void getFriends(final IRequestListener<AppUserArray> listener)
	{	
		DataRequestGetFriends friends = new DataRequestGetFriends(context);
		service.addDataRequest(friends, listener);
	}
}

package dimappers.android.pub;

import net.awl.appgarden.sdk.AppGardenAgent;

public class PersonFinder {
	IPubService service;
	
	PersonFinder(IPubService service)
	{
		this.service = service;
		AppGardenAgent.passExam("CREATED A PERSONFINDER");
	}
	
	public void getFriends(final IRequestListener<AppUserArray> listener)
	{	
		DataRequestGetFriends friends = new DataRequestGetFriends();
		service.addDataRequest(friends, listener);
	}
}

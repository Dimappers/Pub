package dimappers.android.pub;

import java.util.Collection;

import dimappers.android.PubData.PubEvent;
import android.os.IBinder;

public interface IPubService extends IBinder {

	int 					GiveNewSavedEvent(PubEvent event);
	int 					GiveNewSentEvent(PubEvent event);
	
	Collection<PubEvent> 	GetSavedEvents();
	Collection<PubEvent> 	GetSentEvents();
	Collection<PubEvent> 	GetAllInvited();
	PubEvent 				GetNextEvent();
	
	void					RemoveSavedEvent(PubEvent event);
	
}

package dimappers.android.pub;

import java.util.Collection;

import dimappers.android.PubData.PubEvent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

public interface IPubService extends IBinder {

	int 					GiveNewSavedEvent(PubEvent event);
	int 					GiveNewSentEvent(PubEvent event);
	
	Collection<PubEvent> 	GetSavedEvents();
	Collection<PubEvent> 	GetSentEvents();
	Collection<PubEvent> 	GetInvitedEvents();
	PubEvent 				GetNextEvent();
	
	void					RemoveSavedEvent(PubEvent event);
	
	void					PerformUpdate(boolean fullUpdate);
	
	void					Load(String loadedData);
	String 					Save();
	
	boolean					SendingMessage();
	
}

package dimappers.android.pub;

import java.util.Collection;

import com.facebook.android.Facebook;

import dimappers.android.PubData.PubEvent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

public interface IPubService extends IBinder {

	int 					GiveNewSavedEvent(PubEvent event);
	void 					GiveNewSentEvent(PubEvent event, final IRequestListener<PubEvent> listener);
	
	Collection<PubEvent> 	GetSavedEvents();
	Collection<PubEvent> 	GetSentEvents();
	Collection<PubEvent> 	GetInvitedEvents();
	PubEvent 				GetNextEvent();
	
	void					RemoveSavedEvent(PubEvent event);
	
	void					PerformUpdate(boolean fullUpdate);
	
	void					Load(String loadedData);
	String 					Save();
	
	boolean					SendingMessage();
	
	Facebook 				GetFacebook();
	void					Logout();
	
	AppUser					GetActiveUser();
	
	<K, T> void 			addDataRequest(IDataRequest<K, T> request, final IRequestListener<T> listener);	
	
}

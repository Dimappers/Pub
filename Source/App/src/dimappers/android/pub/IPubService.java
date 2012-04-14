package dimappers.android.pub;

import java.util.Collection;
import java.util.HashMap;

import com.facebook.android.Facebook;

import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

public interface IPubService extends IBinder {

	int 									GiveNewSavedEvent(PubEvent event);
	void 									GiveNewSentEvent(PubEvent event, final IRequestListener<PubEvent> listener);
	
	void 									NewEventsRecieved(PubEventArray events);
		
	Collection<PubEvent> 					GetSavedEvents();
	Collection<PubEvent> 					GetSentEvents();
	Collection<PubEvent> 					GetInvitedEvents();
	PubEvent 								GetNextEvent();
	
	void									RemoveEventFromStoredDataAndCancelNotification(PubEvent event);
	
	void									PerformUpdate(boolean fullUpdate);
	
	Facebook 								GetFacebook();
	void									Logout();
	
	AppUser									GetActiveUser();
	
	<K, T extends IXmlable> void 			addDataRequest(IDataRequest<K, T> request, final IRequestListener<T> listener);	
	
	HistoryStore							getHistoryStore();
	
	PubEvent								getEvent(int eventId);
	
	<K, V  extends IXmlable> HashMap<K, V> 	GetGenericStore(String key);
}

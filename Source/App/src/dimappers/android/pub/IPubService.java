package dimappers.android.pub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;

import com.facebook.android.Facebook;

import dimappers.android.PubData.IXmlable;
import dimappers.android.PubData.PubEvent;
import android.os.IBinder;

public interface IPubService extends IBinder {

	int 									GiveNewSavedEvent(PubEvent event);
	void 									GiveNewSentEvent(PubEvent event, final IRequestListener<PubEvent> listener);
	
	void 									NewEventsRecieved(PubEventArray events);
		
	Collection<PubEvent> 					GetSavedEvents();
	Collection<PubEvent> 					GetSentEvents();
	Collection<PubEvent> 					GetInvitedEvents();
	PubEvent 								GetNextEvent();
	
	void 									UpdatePubEvent(PubEvent newEvent);
	
	void									RemoveEventFromStoredDataAndCancelNotification(PubEvent event);
	void 									CancelEvent(final PubEvent event);
	
	void									PerformUpdate(boolean fullUpdate);
	
	Facebook 								GetFacebook();
	void									Logout() throws MalformedURLException, IOException;
	
	AppUser									GetActiveUser();
	
	<K, T extends IXmlable> void 			addDataRequest(IDataRequest<K, T> request, final IRequestListener<T> listener);	
	
	HistoryStore							getHistoryStore();
	
	PubEvent								getEvent(int eventId);
	
	void 									AddEventToHistory(PubEvent event);
	void									DeleteSentEvent(PubEvent event);
	
	<K, V  extends IXmlable> HashMap<K, V> 	GetGenericStore(String key);
}

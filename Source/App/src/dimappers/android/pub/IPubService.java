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
		
	Collection<PubEvent> 					GetSavedEvents(); //TODO: Need to combine/more consistent system
	Collection<PubEvent> 					GetSentEvents();
	Collection<PubEvent> 					GetInvitedEvents();
	PubEvent 								GetNextEvent();
	
	void 									UpdatePubEvent(PubEvent newEvent); //TODO: Shouldn't need this one
	
	void									RemoveEventFromStoredDataAndCancelNotification(PubEvent event); //Used in CurrentEvents, needs to be removed
	
	//New IPubService
	int										SaveEvent(PubEvent event); //Store an event locally (done at generation of event) returns saved id (< 0)
	void									SendEvent(PubEvent event, final IRequestListener<PubEvent> listener); //Send an event to the server, listener
	PubEvent								getEvent(int eventId);
	void									ConfirmEvent(final PubEvent event, final IRequestListener<PubEvent> listener);
	void									CancelEvent(final PubEvent event, final IRequestListener<PubEvent> listener); //
	void									DeleteEvent(final PubEvent event);
	
	void									PerformUpdate(boolean fullUpdate);
	void									ReceiveEvents(PubEventArray events);
	
	Facebook 								GetFacebook();
	AppUser 								GetActiveUser();
	void									Logout() throws MalformedURLException, IOException;
	
	<K, T extends IXmlable> void 			addDataRequest(IDataRequest<K, T> request, final IRequestListener<T> listener);
	
	HistoryStore							getHistoryStore();
	void 									AddEventToHistory(PubEvent event);
	
	<K, V  extends IXmlable> HashMap<K, V> 	GetGenericStore(String key); //Don't like this, want to remove but seems to be vital
}

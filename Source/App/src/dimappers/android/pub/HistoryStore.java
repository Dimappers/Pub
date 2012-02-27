package dimappers.android.pub;

import java.util.ArrayList;
import java.util.List;

import dimappers.android.PubData.PubEvent;

public class HistoryStore {
	
	HistoryStore() {}
	
	public int getAverageNumberOfFriends() {return 10;}
	public List<PubEvent> getPubTrips() {return (List<PubEvent>) new ArrayList<PubEvent>();};
}

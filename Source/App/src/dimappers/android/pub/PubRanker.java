package dimappers.android.pub;

import java.util.ArrayList;
import java.util.List;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.User;

public class PubRanker {

	List<PubLocation> pubList;
	PubEvent event;
	HistoryStore historyStore;
	PubLocation bestSoFar;
	
	PubRanker(List<Place> list, PubEvent event, HistoryStore historyStore) {
		this.historyStore = historyStore;
		
		pubList = new ArrayList<PubLocation>();
		for(Place place : list) {
			pubList.add(new PubLocation((float)place.geometry.location.lat, (float)place.geometry.location.lng, place.name));
		}
		this.event = event;
	}
	
	public PubLocation returnBest() {
		User[] guests = event.GetUserArray();
		for(PubLocation pub : pubList) {
			int rank = 0;
			for(User guest : guests)
			{
				rank += setRank(guest, pub);
			}
			pub.setRank(rank);
			if(bestSoFar==null||bestSoFar.getRank()<rank) {bestSoFar = pub;}
		}
		return bestSoFar;
	}
	
	private int setRank(User guest, PubLocation pub) 
	{
		List<PubEvent> pastEvents = historyStore.getPubTrips();
		int rank = 0;
		for (PubEvent event : pastEvents) 
		{
			if(event.GetHost().equals(guest)) {rank+=2;}
			else 
				{
					User[] guests = event.GetUserArray();
					for (User user : guests)
					{
						if(guest.equals(user)) {rank+=1;}
					}
				}
		}
		return rank;
	}
}

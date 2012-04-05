package dimappers.android.pub;

import java.util.Calendar;
import java.util.List;

import dimappers.android.PubData.PubEvent;

public class TimeFinder {

	HistoryStore historyStore;
	int hour = 0;
	int minutes = 0;
	
	TimeFinder(HistoryStore historyStore) {this.historyStore = historyStore;}
	
	public Calendar chooseTime() {
		findHour();
		Calendar time = Calendar.getInstance();
		time.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DATE), //Using current date 
				hour, minutes, 0); 															 //Using time based on average start time of previous pub trips
		if(Calendar.getInstance().after(time)) {time.add(Calendar.DATE, 1);} 
		return time;
	}
	
	private void findHour() {
		List<PubEvent> pubEvents = historyStore.getPubTrips();
		if(pubEvents.size()==0||pubEvents==null){hour = 19; minutes = 0;}
		else {
			hour = 0;
			minutes = 0;
			for (PubEvent pubEvent : pubEvents) {
				hour += pubEvent.GetStartTime().get(Calendar.HOUR_OF_DAY);
				minutes += pubEvent.GetStartTime().get(Calendar.MINUTE);
			}
			hour = (int)(hour/pubEvents.size());
			minutes = roundToNearestQuarter();
		}
	}

	private int roundToNearestQuarter() {
		if(minutes<=7) {return 0;}
		else if(minutes<=22) {return 15;}
		else if(minutes<=37){return 30;}
		else if(minutes<=52){return 45;}
		else {return 0;}
	}

}

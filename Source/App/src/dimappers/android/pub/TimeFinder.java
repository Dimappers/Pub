package dimappers.android.pub;

import java.util.Calendar;

public class TimeFinder {

	TimeFinder() {}
	
	public Calendar chooseTime() {
		// TODO: Implement method properly
		Calendar time = Calendar.getInstance();
		time.set(time.get(Calendar.YEAR), 1 + time.get(Calendar.MONTH), time.get(Calendar.DATE), //Using current date 
				19, 0, 0); 																		 //Using time based on average start time of previous pub trips
		return time;
	}

}

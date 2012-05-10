package dimappers.android.PubData;

import java.util.HashMap;

public enum UpdateType {
	newEvent(1), //is a new event
	updatedEvent(2), //existing event has been updated since last time user got it
	confirmed(3), //has been confirmed since last time user got event 
	newEventConfirmed(4), // Event is new and has been confirmed since last update
	updatedConfirmed(5), //Event has changed and since been confirmed 
	confirmedUpdated(6), //Has been confirmed, then updated (After being confirmed) since last update
	userReplied(7), //One or more users has replied
	noChangeSinceLastUpdate(8); //The event is not different since last update (=> user has called for a full update)
	
	int value;
	static HashMap<Integer, UpdateType> typeNumber = new HashMap<Integer, UpdateType>();
	
	static {
		for (int i=1; i <= UpdateType.values().length; ++i) {
			typeNumber.put(i, UpdateType.values()[i]);			
		}
	}
	
	UpdateType(int value) {
		this.value = value;
	}
	
	public static UpdateType getType(int i) {
		return typeNumber.get(i);
	}
	
	public int getValue() { return value; }

}

package dimappers.android.PubData;

public enum UpdateType {
	newEvent, //is a new event
	updatedEvent, //existing event has been updated since last time user got it
	confirmed, //has been confirmed since last time user got event 
	newEventConfirmed, // Event is new and has been confirmed since last update
	updatedConfirmed, //Event has changed and since been confirmed 
	confirmedUpdated, //Has been confirmed, then updated (After being confirmed) since last update
	noChangeSinceLastUpdate //The event is not different since last update (=> user has called for a full update)
}

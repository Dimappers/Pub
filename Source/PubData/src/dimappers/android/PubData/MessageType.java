package dimappers.android.PubData;

public enum MessageType {
	
	/* 						newPubEventMessage
	 * Use: 							For creating a new pub event
	 * Class to send to server: 		PubEvent (with host as your user)
	 * Class the server will send back: Acknowledgement saying the global id of that event 
	 * 									(which should be used in future request
	 */
	newPubEventMessage,
	
	/* 						refreshMessage
	 * Use: 							Gets new events and updates information about events 
	 * 									this user has already retrieved
	 * Class to send to the server: 	Guest
	 * Class the server will send back: RefreshData which contains an array of events that 
	 * 									are either new or need updating
	 */
	refreshMessage, 
	/* 						respondMessage
	 * Use: 							Tells the server the respond to a specific event
	 * Class to send to the server: 	EventResponse - containing true if going and the global 
	 * 									id of the event
	 * Class the server will send back: Nothing
	 */
	respondMessage,
	/* 						updateMessage
	 * Use: 							Tells the server to update information of an event 
	 * 									(eg change in time) 
	 * Class to send to the server: 	UpdateData 
	 * Class the server will send back: Nothing
	 */
	updateMessage
}

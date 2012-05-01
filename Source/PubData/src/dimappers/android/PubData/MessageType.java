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
	 * Class to send to the server: 	RefreshData which contains the user & whether to do a full refresh
	 * Class the server will send back: RefreshResponse which contains an array of PubEvent ids which are  
	 * 									either new or need updating  along with type of change
	 */
	refreshMessage,
	
	/* 						refreshEventMessage
	 * Use: 							Gets latest about specific events 
	 * Class to send to the server: 	RefreshEventData which contains the user & id of event
	 * Class the server will send back: PubEvent 
	 */
	refreshEventMessage,
	
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
	updateMessage,
	
	/*						confirmMessage		
	 * User:							Tells the invitees if the event 'is on' or not
	 * Class to send to the server:		Confirm Message
	 * Class the server will send back:	None - but should send this message on to all the guests
	 */
	confirmMessage,
	
	unknownMessageType
}

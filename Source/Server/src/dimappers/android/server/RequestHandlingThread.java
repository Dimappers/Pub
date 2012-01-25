package dimappers.android.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.LinkedList;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;

public class RequestHandlingThread extends Thread{

	public static final boolean IsDebug = true;

	RequestHandlingThread(Socket clientSocket) {
		super();
		System.out.println("Thread ID: " + this.getId());
		handleRequest(clientSocket);
	}

	public void handleRequest(Socket clientSocket) {
		//Deserialise data in to classes - in reality we will have to send some messages before explaining what is coming 
		ObjectInputStream deserialiser = null;
		ObjectOutputStream serialiser = null;
		MessageType message = null;
		try
		{
			deserialiser = new ObjectInputStream(clientSocket.getInputStream());
			serialiser = new ObjectOutputStream(clientSocket.getOutputStream());
			message = (MessageType)deserialiser.readObject();

		} 
		catch (IOException e)
		{
			System.out.println("Error decoding class: " + e.getMessage());
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Error deserialising class: "+ e.getMessage());
		}
		
		switch(message)
		{
		case newPubEventMessage:
		{
			// TOMS JOB
			try
			{
				NewEventMessageReceived(deserialiser, serialiser);
			}
			catch(ServerException e)
			{
				System.out.println("ERROR: " + e.GetExceptionType().toString());
			}
			break;
		}

		case refreshMessage:
		{
			// MARKS JOB : Sent: RefreshData Returns Array of events
			RefreshMessageReceived(deserialiser, serialiser);
			break;
		}

		case respondMessage:
		{
			// TOMS JOB
			RespondMessageReceived(deserialiser, serialiser);
			break;
		}

		case updateMessage:
		{
			// MARKS JOB : Gets UpdateRequest Returns nothin'
			UpdateMessageReceived(deserialiser, serialiser);
			break;
		}

		default:
			System.out.println("Message type not recognised :(");
			break;
		}
	}

	//Message handling functions
	private static void NewEventMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws ServerException
	{
		if(IsDebug)
		{
			System.out.println("Received new PubEvent message");
		}
		
		PubEvent event;
		try {
			event = (PubEvent)connectionStreamIn.readObject();
		} 
		catch (Exception e) {
			try {
				connectionStreamOut.writeObject(new AcknoledgementData(-1));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				throw new ServerException(ExceptionType.SendingErrorBack);
			}
			
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.ReadingObjectNewEvent);
			} else if(e instanceof ClassNotFoundException) {
				//Data in the wrong format
				throw new ServerException(ExceptionType.CastingObjectNewEvent);
			} else if(e instanceof StreamCorruptedException) {
				
			}
			throw new ServerException(ExceptionType.UnknownError);
		}

		if(IsDebug)
		{
			System.out.println("PubEvent at location: " + event.GetPubLocation().toString());
		}

		int pubEventId = EventManager.AddNewEvent(event);

		if(IsDebug)
		{
			System.out.println("Event added with id: " + pubEventId);
		}

		//Go through users and add event to them
		for(User user : event.GetUsers())
		{
			UserManager.addUser(user);
			UserManager.addEvent(user, pubEventId);
		}
		
		UserManager.markAsUpToDate(event.GetHost(), pubEventId);

		connectionStreamOut.writeObject(new AcknoledgementData(pubEventId));
	}

	private static void RefreshMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws IOException, ClassNotFoundException
	{
		RefreshData refresh = (RefreshData)connectionStreamIn.readObject();
		LinkedList<Integer> refreshEventIds;
		if (refresh.isFullUpdate()) {
			// If true, returns all the events, otherwise just the events that need refreshing
			refreshEventIds = UserManager.getFullUpdate(refresh.getUser());
		}
		else {
			refreshEventIds = UserManager.getUpdate(refresh.getUser());
		}
		
		UserManager.markAllAsUpToDate(refresh.getUser());
		
		// Create an array to fit all needed events in
		PubEvent[] refreshEvents = new PubEvent[refreshEventIds.size()];
		int eventCounter = 0;

		// Create an Iterator and iterate through each eventId, adding the appropriate event to the array
		for(int event : refreshEventIds)
		{
			refreshEvents[eventCounter++] = EventManager.GetPubEvent(event);
		}

		// Return the array of events that need updating
		connectionStreamOut.writeObject(refreshEvents);
	}

	private static void RespondMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws IOException, ClassNotFoundException
	{
		ResponseData response = (ResponseData)connectionStreamIn.readObject();

		//Update the event file
		PubEvent event = EventManager.GetPubEvent(response.GetEventId());
		event.UpdateUserStatus(response.GetUser(), response.GetIsGoing());

		for(User user : event.GetUsers())
		{
			if(!user.equals(response.GetUser()))
			{
				//Tell that user they need an update
				UserManager.markForUpdate(user, response.GetEventId());
			}
		}
	}

	private static void UpdateMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws IOException, ClassNotFoundException
	{
		// Gets the event given the update data
		UpdateData update = (UpdateData)connectionStreamIn.readObject();
		PubEvent event = EventManager.GetPubEvent(update.getEventId());

		// Checks if the start time needs amending
		if (update.getStartTime() != null) {
			event.SetStartTime(update.getStartTime());
		}

		// Checks if the Pub Location needs amending
		if (update.getPubLocation() != null) {
			event.SetPubLocation(update.getPubLocation());
		}

		/* Checks if there are any guests that need adding, if so, adds the guest to the event and puts the event
		 * in the Users event list
		 */

		if (!update.getUsers().isEmpty()) {
			LinkedList<User> users = update.getUsers();
			// Add the guests to the event and the event to the guests arrays
			for(User user : users)
			{
				event.AddUser(user);
				UserManager.addEvent(user, update.getEventId());
			}
		}
		
		//Tell all the users invited to this event that they need updating
		for(User user : event.GetUsers())
		{
			if(!user.isEqual(event.GetHost()))
			{
				UserManager.markForUpdate(user, event.GetEventId());
			}
		}
	}
}

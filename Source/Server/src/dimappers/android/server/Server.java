package dimappers.android.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.io.*;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.ResponseData;

public class Server {

	/**
	 * @param args
	 */
	
	private static boolean serverRunning = true;
	private static final int PORT = 2085;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		EventManager.InitFromScratch(); 
		System.out.println("Server running on port " + PORT);
		ServerSocket serverSocket = null;
		UserManager.init();
		
		//Create the socket to listen to
		try
		{
			serverSocket = new ServerSocket(PORT);
		} 
		catch (IOException e)
		{
			System.out.println("Error listening to port: " + e.getMessage());
		}
		
		
		while(serverRunning)
		{
			//Create the socket to receive data from upon connection
			Socket clientSocket = null;
			try
			{
				clientSocket = serverSocket.accept();
			} 
			catch (IOException e)
			{
				System.out.println("Error accepting connection: " + e.getMessage());
			}
			
			System.out.println("Data recieved");
			
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
					NewEventMessageReceived(deserialiser, serialiser);
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
		
		System.out.println("Server closing...");
	}
	
	public static void TerminateServer()
	{
		serverRunning = false;
	}
	
	//Message handling functions
	private static void NewEventMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws IOException, ClassNotFoundException 
	{
		PubEvent event = (PubEvent)connectionStreamIn.readObject();
		int pubEventId = EventManager.AddNewEvent(event);
		
		//Go through users and add event to them
		for(User user : event.GetGuests())
		{
			ServerUser sUser = null; //AddOrGetUser(user.name);
			//sUser.AddEvent(pubEventId);
		}
		
		connectionStreamOut.writeObject(new AcknoledgementData(pubEventId));
	}
	
	private static void RefreshMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws IOException, ClassNotFoundException
	{
		RefreshData refresh = (RefreshData)connectionStreamIn.readObject();
		LinkedList<Integer> refreshEventIds;
		if (refresh.isFullUpdate()) {
			// If true, returns all the events, otherwise just the events that need refreshing
			refreshEventIds = UserManager.getFullUpdate(refresh.getUserId());
		}
		else {
			refreshEventIds = UserManager.getUpdate(refresh.getUserId());
		}
		// Create an array to fit all needed events in
		PubEvent[] refreshEvents = new PubEvent[refreshEventIds.size()];
		int eventCounter = 0;
		
		// Create an Iterator and iterate through each eventId, adding the appropriate event to the array
		Iterator<Integer> iter = refreshEventIds.iterator();
		int event;
		while (true) {
			try {
				event = iter.next().intValue();
				refreshEvents[eventCounter++] = EventManager.GetPubEvent(event);
			} catch (NoSuchElementException e) {
				break;
			}
		}
		
		// Return the array of events that need updating
		connectionStreamOut.writeObject(refreshEvents);
	}
	
	private static void RespondMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws IOException, ClassNotFoundException
	{
		ResponseData response = (ResponseData)connectionStreamIn.readObject();
		
		//Update the event file
		PubEvent event = EventManager.GetPubEvent(response.GetEventId());
		event.UpdateGuestStatus(response.GetGuest(), response.GetIsGoing());
		
		for(User user : event.GetGuests())
		{
			if(!user.equals(response.GetGuest()))
			{
				//Tell that user they need an update
				ServerUser sUser = null; //GetFromUserManager
				sUser.NotifyEventUpdated(response.GetEventId());
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
		
		if (!update.getGuests().isEmpty()) {
			LinkedList<User> guests = update.getGuests();
			// Add the guests to the event and the event to the guests arrays
			Iterator<User> iter = guests.iterator();
			while (true) {
				try {
					User guest = iter.next();
					event.AddGuest(guest);
					UserManager.addEvent(guest.getName(), update.getEventId());
				} catch (NoSuchElementException e) {
					break;
				}
			}
		}
	}

}

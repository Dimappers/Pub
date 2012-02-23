package dimappers.android.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.Calendar;
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
		MessageType message = MessageType.unknownMessageType;
		try
		{
			deserialiser = new ObjectInputStream(clientSocket.getInputStream());
			serialiser = new ObjectOutputStream(clientSocket.getOutputStream());
			message = (MessageType)deserialiser.readObject();

		} 
		catch (Exception e)
		{
			ServerException sException = new ServerException(ExceptionType.MessageReceivedUnknownError, e);
			if(e instanceof IOException) {
				//Error reading object
				sException = new ServerException(ExceptionType.MessageReceivedReadingObject, e);
			} else if(e instanceof ClassNotFoundException) {
				//Data in the wrong format
				sException = new ServerException(ExceptionType.MessageReceivedCastingObject, e);
			} else if(e instanceof StreamCorruptedException) {
				sException = new ServerException(ExceptionType.MessageReceivedStreamCorrupted, e);
			}
			
			HandleException(sException);
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
				HandleException(e);
			}
			break;
		}

		case refreshMessage:
		{
			// MARKS JOB : Sent: RefreshData Returns Array of events
			try
			{
				RefreshMessageReceived(deserialiser, serialiser);
			}
			catch(ServerException e)
			{
				HandleException(e);
			}
			break;
		}

		case respondMessage:
		{
			// TOMS JOB
			try
			{
				RespondMessageReceived(deserialiser, serialiser);
			} catch (ServerException e)
			{
				HandleException(e);
			}
			break;
		}

		case updateMessage:
		{
			// MARKS JOB : Gets UpdateRequest Returns nothin'
			try
			{
				UpdateMessageReceived(deserialiser, serialiser);
			} catch (ServerException e)
			{
				HandleException(e);
			}
			break;
		}

		case unknownMessageType:
		default:
			HandleException(new ServerException(ExceptionType.UnknownMessageTypeError));
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
				System.out.println("Error!");
				connectionStreamOut.writeObject(new AcknoledgementData(-1));
			} catch (Exception e1) {

				throw new ServerException(ExceptionType.NewEventSendingErrorBack, e1);
			}
			
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.NewEventReadingObject, e);
			} else if(e instanceof ClassNotFoundException) {
				//Data in the wrong format
				throw new ServerException(ExceptionType.NewEventCastingObject, e);
			} else if(e instanceof StreamCorruptedException) {
				throw new ServerException(ExceptionType.NewEventStreamCorrupted, e);
			}
			throw new ServerException(ExceptionType.NewEventUnknownError, e);
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
		
		try
		{
			connectionStreamOut.writeObject(new AcknoledgementData(pubEventId));
		}
		catch(Exception e)
		{
			try {
				connectionStreamOut.writeObject(new AcknoledgementData(-1));
			} catch (Exception e1) {
				throw new ServerException(ExceptionType.NewEventSendingErrorBack, e1);
			}
			
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.NewEventSendingAcknoledgementBack, e);
			} else if(e instanceof StreamCorruptedException) {
				throw new ServerException(ExceptionType.NewEventStreamCorrupted, e);
			}
			throw new ServerException(ExceptionType.NewEventUnknownError, e);
		}
	}

	private static void RefreshMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws ServerException
	{
		RefreshData refresh;
		try
		{
			refresh = (RefreshData)connectionStreamIn.readObject();
		} catch (Exception e)
		{
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.RefreshReadingObject, e);
			} else if(e instanceof ClassNotFoundException) {
				//Data in the wrong format
				throw new ServerException(ExceptionType.RefreshCastingObject, e);
			} else if(e instanceof StreamCorruptedException) {
				throw new ServerException(ExceptionType.RefreshStreamCorrupted, e);
			}
			throw new ServerException(ExceptionType.RefreshUnknownError, e);
		} 
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
		try
		{
			connectionStreamOut.writeObject(refreshEvents);
		} catch (Exception e)
		{
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.RefreshReadingObject, e);
			} else if(e instanceof StreamCorruptedException) {
				throw new ServerException(ExceptionType.RefreshStreamCorrupted, e);
			}
			throw new ServerException(ExceptionType.RefreshUnknownError, e);
		}
	}

	private static void RespondMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws ServerException
	{
		ResponseData response;
		try
		{
			response = (ResponseData)connectionStreamIn.readObject();
		} catch (Exception e)
		{
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.RespondReadingObject, e);
			} else if(e instanceof ClassNotFoundException) {
				//Data in the wrong format
				throw new ServerException(ExceptionType.RespondCastingObject, e);
			} else if(e instanceof StreamCorruptedException) {
				throw new ServerException(ExceptionType.RespondStreamCorrupted, e);
			}
			throw new ServerException(ExceptionType.RespondUnknownError, e);
		} 

		//Update the event file
		PubEvent event = EventManager.GetPubEvent(response.GetEventId());
		event.UpdateUserStatus(response);

		for(User user : event.GetUsers())
		{
			if(!user.equals(response.GetUser()))
			{
				//Tell that user they need an update
				UserManager.markForUpdate(user, response.GetEventId());
			}
		}
	}

	private static void UpdateMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream connectionStreamOut) throws ServerException
	{
		// Gets the event given the update data
		UpdateData update;
		try
		{
			update = (UpdateData)connectionStreamIn.readObject();
		} catch (Exception e)
		{
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.RespondReadingObject, e);
			} else if(e instanceof ClassNotFoundException) {
				//Data in the wrong format
				throw new ServerException(ExceptionType.RespondCastingObject, e);
			} else if(e instanceof StreamCorruptedException) {
				throw new ServerException(ExceptionType.RespondStreamCorrupted, e);
			}
			throw new ServerException(ExceptionType.RespondUnknownError, e);
		} 
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
			if(!user.equals(event.GetHost()))
			{
				UserManager.markForUpdate(user, event.GetEventId());
			}
		}
	}
	
	private static void HandleException(ServerException e)
	{
		try
		{
			PrintWriter errorWriter = new PrintWriter(new File("ServerErrorLog.txt"));
			errorWriter.write("Error: " + e.GetExceptionType().toString() + " occured at time: " + Calendar.getInstance().getTime());
			if(e.GetOriginalException() != null)
			{
				errorWriter.write("Stack trace: ");
			
				for(StackTraceElement stackTraceRow : e.GetOriginalException().getStackTrace())
				{
					errorWriter.write(stackTraceRow.toString());
				}
			}
			
			errorWriter.flush();
			errorWriter.close();
		} catch (FileNotFoundException e1)
		{
			System.out.println("Well... this is very much bad");
		}
		
		//TODO: Handle specific error messages?
		switch(e.GetExceptionType())
		{
		case EventManagerErrorCreatingDatabase:
			break;
		case EventManagerErrorOpeningDatabase:
			break;
		case EventManagerErrorReadingDatabase:
			break;
		case EventManagerNoSpace:
			break;
		case EventManagerNoSuchEvent:
			break;
		case NewEventCastingObject:
			break;
		case NewEventReadingObject:
			break;
		case NewEventSendingAcknoledgementBack:
			break;
		case NewEventSendingErrorBack:
			break;
		case NewEventStreamCorrupted:
			break;
		case NewEventUnknownError:
			break;
		case RefreshCastingObject:
			break;
		case RefreshReadingObject:
			break;
		case RefreshStreamCorrupted:
			break;
		case RefreshUnknownError:
			break;
		case RespondCastingObject:
			break;
		case RespondReadingObject:
			break;
		case RespondStreamCorrupted:
			break;
		case RespondUnknownError:
			break;
		case UpdateCastingObject:
			break;
		case UpdateReadingObject:
			break;
		case UpdateStreamCorrupted:
			break;
		case UpdateUnknownError:
			break;
		
		}
	}
}

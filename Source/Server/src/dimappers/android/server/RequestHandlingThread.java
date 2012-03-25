package dimappers.android.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.LinkedList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.RefreshResponse;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;

public class RequestHandlingThread extends Thread{

	private final static String endString  = "</Message>";
	public static final boolean IsDebug = true;

	RequestHandlingThread(Socket clientSocket) {
		super();
		System.out.println("Thread ID: " + this.getId());
		handleRequest(clientSocket);
	}

	public void handleRequest(Socket clientSocket) {
		//Deserialise data in to classes - in reality we will have to send some messages before explaining what is coming 
		
		Document doc = null;
		
		MessageType message = MessageType.unknownMessageType;
		try
		{
			doc = readMessageToEnd(clientSocket.getInputStream());
		} 
		catch (Exception e)
		{
			System.out.println("Error recieving the document");
			ServerException sException = new ServerException(ExceptionType.MessageReceivedUnknownError, e);
			HandleException(sException);
		}
		
		if (doc != null) {
			message = MessageType.valueOf(doc.getRootElement().getChild("MessageType").getText());
		}
		
		
		switch(message)
		{
		case newPubEventMessage:
		{
			// TOMS JOB
			try
			{
				System.out.println("Creating new event");
				NewEventMessageReceived(doc, clientSocket);
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
				System.out.println("Refreshing client");
				RefreshMessageReceived(doc, clientSocket);
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
				System.out.println("Response recieved");
				RespondMessageReceived(doc, clientSocket);
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
				System.out.println("Update recieved");
				UpdateMessageReceived(doc, clientSocket);
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
	private static void NewEventMessageReceived(Document doc, Socket clientSocket) throws ServerException
	{
		if(IsDebug)
		{
			System.out.println("Received new PubEvent message");
		}
		
		PubEvent event;
		event = new PubEvent(doc.getRootElement().getChild(PubEvent.class.getSimpleName()));

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
		
		AcknoledgementData ack = new AcknoledgementData(pubEventId);
		
		// Creates the XML Document tree that is being returned
		Element root = new Element("root");
		Document returnDocument = new Document(root);
		root.addContent(ack.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		
		try
		{
			OutputStream outStream = clientSocket.getOutputStream();
			outputter.output(returnDocument, outStream);
			outStream.flush();
			System.out.println("Closing connection");
			clientSocket.close();
		}
		catch(Exception e)
		{
			try {
				// Handle the change that is caused to this exception
				OutputStream outStream = clientSocket.getOutputStream();
				outputter.output(new Document(), outStream);
				outStream.flush();
				//outStream.close();
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

	private static void RefreshMessageReceived(Document doc, Socket clientSocket) throws ServerException
	{
		RefreshData refresh;
		refresh = new RefreshData(doc.getRootElement().getChild(RefreshData.class.getSimpleName()));

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

		RefreshResponse response = new RefreshResponse(refreshEvents);
		
		// Creates the XML Document tree that is being returned
		Element root = new Element("root");
		Document returnDoc = new Document(root);
		root.addContent(response.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		
		// Return the array of events that need updating
		try
		{
			outputter.output(returnDoc, clientSocket.getOutputStream());
		} catch (Exception e)
		{
			if(e instanceof IOException) {
				//Error reading object
				throw new ServerException(ExceptionType.RefreshReadingObject, e);
			}
			throw new ServerException(ExceptionType.RefreshUnknownError, e);
		}
	}

	private static void RespondMessageReceived(Document doc, Socket clientSocket) throws ServerException
	{
		ResponseData response;
		response = new ResponseData(doc.getRootElement().getChild(ResponseData.class.getSimpleName()));

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

	private static void UpdateMessageReceived(Document doc, Socket clientSocket) throws ServerException
	{
		// Gets the event given the update data
		UpdateData update;
		update = new UpdateData(doc.getRootElement().getChild(UpdateData.class.getSimpleName()));

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
			System.out.println("Error: " + e.GetExceptionType().toString() + " occured at time: " + Calendar.getInstance().getTime());
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

	private static Document readMessageToEnd(InputStream inStream) throws IOException, JDOMException
	{
		SAXBuilder docBuilder = new SAXBuilder();
		int nextByte = inStream.read();
		StringBuilder sBuilder = new StringBuilder();
		while(nextByte != -1)
		{
			sBuilder.append((char)nextByte);
			if(sBuilder.length() >= endString.length() && sBuilder.toString().endsWith(endString))
			{
				break;
			}
			else
			{
				nextByte = inStream.read();
			}
		}
		System.out.println(sBuilder.toString());
		StringReader reader = new StringReader(sBuilder.toString());
		return docBuilder.build(reader);
	}
}

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
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.ConfirmMessage;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.RefreshEventMessage;
import dimappers.android.PubData.RefreshEventResponseMessage;
import dimappers.android.PubData.RefreshResponse;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.UpdateType;
import dimappers.android.PubData.User;
import dimappers.android.PubData.UserStatus;

public class RequestHandlingThread extends Thread{

	private final static String endString  = "</Message>";
	public static final boolean IsDebug = true;

	RequestHandlingThread(Socket clientSocket) {
		super();
		System.out.println("Thread ID: " + this.getId());
		try {
			handleRequest(clientSocket);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleRequest(Socket clientSocket) throws SQLException {
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
			message = MessageType.valueOf(doc.getRootElement().getChild(MessageType.class.getSimpleName()).getText());
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
		
		case refreshEventMessage : {
			try
			{
				System.out.println("Refresh individual event recieved");
				RefreshEventMessageReceived(doc, clientSocket);
			} catch (ServerException e)
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

		case confirmMessage:
			try
			{
				System.out.println("Confirm/Deny received");
				ItsOnMessageReceived(doc, clientSocket);
			}
			catch(ServerException e)
			{
				HandleException(e);
			}
			break;
		case unknownMessageType:
		default:
			HandleException(new ServerException(ExceptionType.UnknownMessageTypeError));
			break;
		}
	}

	//Message handling functions
	private static void NewEventMessageReceived(Document doc, Socket clientSocket) throws ServerException, SQLException
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
		Element root = new Element("Message");
		Document returnDocument = new Document(root);
		root.addContent(ack.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		
		try
		{
			OutputStream outStream = clientSocket.getOutputStream();
			//outStream.write(b, off, len)
			outputter.output(returnDocument, outStream);
			outStream.flush();
			clientSocket.close();
		}
		catch(Exception e)
		{
			try {
				// Handle the change that is caused to this exception
				OutputStream outStream = clientSocket.getOutputStream();
				outputter.output(new Document(), outStream);
				outStream.flush();
				outStream.close();
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

	private static void RefreshMessageReceived(Document doc, Socket clientSocket) throws ServerException, SQLException
	{
		RefreshData refresh;
		refresh = new RefreshData(doc.getRootElement().getChild(RefreshData.class.getSimpleName()));

		Set<Integer> refreshEventIds;
		if (refresh.isFullUpdate()) {
			// If true, returns all the events, otherwise just the events that need refreshing
			refreshEventIds = UserManager.getFullUpdate(refresh.getUser());
		}
		else {
			refreshEventIds = UserManager.getUpdate(refresh.getUser());
		}

		RefreshResponse response = new RefreshResponse(refreshEventIds);
		
		// Creates the XML Document tree that is being returned
		Element root = new Element("Message");
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
	
	private static void RefreshEventMessageReceived(Document doc, Socket clientSocket) throws ServerException, SQLException
	{
		RefreshEventMessage refreshEventMessage;
		refreshEventMessage = new RefreshEventMessage(doc.getRootElement().getChild(RefreshEventMessage.class.getSimpleName()));
			
		UpdateType updateType = UserManager.getUpdateType(refreshEventMessage.getUser(), refreshEventMessage.getEventId());
		PubEvent eventToReturn = EventManager.GetPubEvent(refreshEventMessage.getEventId());
		RefreshEventResponseMessage returnMessage = new RefreshEventResponseMessage(eventToReturn, updateType);
		
		Element root = new Element("Message");
		Document returnDoc = new Document(root);
		root.addContent(returnMessage.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		
		// Return the latest info about the specified event
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
		
		UserManager.markAsUpToDate(refreshEventMessage.getUser(), refreshEventMessage.getEventId());
	}

	private static void RespondMessageReceived(Document doc, Socket clientSocket) throws ServerException, SQLException
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
				UserManager.markForUserResponse(user, response.GetEventId());
			}
		}
		
		UpdateType updateType = UserManager.getUpdateType(response.GetUser(), event.GetEventId());
		
		RefreshEventResponseMessage latestEventReturnMessage = new RefreshEventResponseMessage(event, updateType);
		
		Element root = new Element("Message");
		Document returnDoc = new Document(root);
		root.addContent(latestEventReturnMessage.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		
		// Return the latest info about the specified event
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
		
		UserManager.markAsUpToDate(response.GetUser(), event.GetEventId());		
	}

	private static void UpdateMessageReceived(Document doc, Socket clientSocket) throws ServerException, SQLException
	{
		// Gets the event given the update data
		UpdateData update;
		update = new UpdateData(doc.getRootElement().getChild(UpdateData.class.getSimpleName()));

		EventChange eventChange = EventChange.timeNotChanged;
		Calendar oldEventTime;
		
		PubEvent event = EventManager.GetPubEvent(update.getEventId());

		oldEventTime = event.GetStartTime();
		// Checks if the start time needs amending
		if (update.getStartTime() != null) {
			if(event.GetStartTime().after(update.getStartTime()))
			{
				//original event time is after the new time, there for the new time is earlier
				eventChange = EventChange.earlierTime;
			}
			else if(event.GetStartTime().before(update.getStartTime()))
			{
				eventChange = EventChange.laterTime;
			}
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
				UserManager.addUser(user);
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
			else
			{
				//We are the host, so we are obviously free for this time
				ResponseData response = new ResponseData(user, event.GetEventId(), true, event.GetStartTime(), "");
				event.UpdateUserStatus(response);
			}
		}
		
		UpdateType updateType = UserManager.getUpdateType(event.GetHost(), event.GetEventId());
		
		RefreshEventResponseMessage latestEventReturnMessage = new RefreshEventResponseMessage(event, updateType);
		
		Element root = new Element("Message");
		Document returnDoc = new Document(root);
		root.addContent(latestEventReturnMessage.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		
		// Return the latest info about the specified event
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
		
		UserManager.markAsUpToDate(event.GetHost(), event.GetEventId());
	}
	
	private static void ItsOnMessageReceived(Document doc, Socket clientSocket) throws ServerException, SQLException
	{
		ConfirmMessage message = new ConfirmMessage(doc.getRootElement().getChild(ConfirmMessage.class.getSimpleName()));
		
		EventManager.ConfirmTrip(message.getEventId(), message.getEventStatus());
		
		PubEvent event = EventManager.GetPubEvent(message.getEventId());
		for(User user : event.GetUsers())
		{
			if(!user.equals(event.GetHost()))
			{
				UserManager.markForConfirmed(user, message.getEventId());
			}
		}
		
		UpdateType updateType = UserManager.getUpdateType(event.GetHost(), event.GetEventId());
		
		RefreshEventResponseMessage latestEventReturnMessage = new RefreshEventResponseMessage(event, updateType);
		
		Element root = new Element("Message");
		Document returnDoc = new Document(root);
		root.addContent(latestEventReturnMessage.writeXml());
		
		XMLOutputter outputter = new XMLOutputter();
		
		// Return the latest info about the specified event
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
		
		UserManager.markAsUpToDate(event.GetHost(), event.GetEventId());
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
		case MessageReceivedCastingObject:
			break;
		case MessageReceivedReadingObject:
			break;
		case MessageReceivedStreamCorrupted:
			break;
		case MessageReceivedUnknownError:
			break;
		case ServerUserNoSuchEvent:
			break;
		case UnknownMessageTypeError:
			break;
		case UserManagerErrorCreatingDatabase:
			break;
		case UserManagerErrorOpeningDatabase:
			break;
		case UserManagerErrorReadingDatabase:
			break;
		case UserManagerMaxUsers:
			break;
		case UserManagerNoSuchUser:
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
	
	enum EventChange
	{
		laterTime,
		earlierTime,
		timeNotChanged
	}
}

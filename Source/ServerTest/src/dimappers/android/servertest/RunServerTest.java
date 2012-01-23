package dimappers.android.servertest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.PubTripState;

public class RunServerTest
{

	/**
	 * @param args
	 */
	
	private static final TestType testType =  TestType.CreateWithGuestRespond;
	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
		//These tests are invalid and for the old server, left for posterity
		// TODO Auto-generated method stub
		
		switch(testType)
		{
		case CreateGet:
			{
				int eventId = createPubEventTest(CreatePubEvent());
				PubEvent[] events = RunRefreshMessageTest(CreateHost(), false);
				
				break;
			}
			
		case CreateUpdate:
			{
				int eventId = createPubEventTest(CreatePubEvent());
				UpdateData myTestUpdate = new UpdateData(eventId, null, new PubLocation(0,0, "Satchwells"));
				RunUpdateMessage(myTestUpdate);
				
				break;
			}
			
		case CreateWithGuestGuestCheck:
			{
				int eventId = createPubEventTest(CreatePubEventWithGuest());
				
				PubEvent[] guestEvents = RunRefreshMessageTest(CreateGuest(), false);
				PubEvent[] hostEvents = RunRefreshMessageTest(CreateHost(), false);
				
				for(PubEvent event : guestEvents)
				{
					System.out.println("Retrived event for guest: " + event.GetPubLocation().toString());
				}
				
				break;
			}
			
		case CreateWithGuestRespond:
			{
				int eventId = createPubEventTest(CreatePubEventWithGuest());
				
				PubEvent[] guestEvents = RunRefreshMessageTest(CreateGuest(), false);
				int yesNo = 0;
				for(PubEvent event : guestEvents)
				{
					RunRespondMessage(event.GetEventId(), yesNo % 2 == 0);
				}
				
				break;
			}
		}
	}
	
	private static User CreateHost()
	{
		return new User(0);
	}
	
	private static User CreateGuest()
	{
		return new User(1);
	}
	
	private static PubEvent CreatePubEvent()
	{
		return new PubEvent(new Date(100000), new PubLocation(42, 36, "Spoons Leam"), CreateHost());
	}
	
	private static PubEvent CreatePubEventWithGuest()
	{
		PubEvent event = new PubEvent(new Date(50000), new PubLocation(21,21, "Robins WelL"), CreateHost());
		event.AddUser(CreateGuest());
		
		return event; 
	}
	
	private static Socket GetSendSocket() throws UnknownHostException, IOException
	{
		return new Socket(InetAddress.getByName("localhost"), 2085);
	}
	
	private static int createPubEventTest(PubEvent event) throws ClassNotFoundException
	{
		System.out.println("Running newEventMessage test");
		Socket sendSocket = null;
		
		//Data before sent
		System.out.println("Sending the following data:");
		System.out.println(event.GetStartTime().toString());
		System.out.println(event.GetPubLocation().toString());
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = GetSendSocket();
		}
		catch(UnknownHostException e)
		{
			System.out.println("Unknown host: " + e.getMessage());
			return -1;
		}
		catch(IOException e)
		{
			System.out.println("IOException: " + e.getMessage());
			return -2;
		}
		
		//Serialise the object for transmission
		ObjectOutputStream serialiser = null;
		ObjectInputStream deserialiser = null;
		try
		{
			serialiser = new ObjectOutputStream(sendSocket.getOutputStream());
			deserialiser = new ObjectInputStream(sendSocket.getInputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return -3;
		}
		
		try
		{
			MessageType t = MessageType.newPubEventMessage;
			serialiser.writeObject(t);
			serialiser.writeObject(event);
			serialiser.flush();
			System.out.println("Data sent");
			AcknoledgementData globalEventId = (AcknoledgementData)deserialiser.readObject();
			
			System.out.println("Event ID: " + globalEventId.globalEventId);
			
			return globalEventId.globalEventId;
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
			return -4;
		}
	}
	
	private static PubEvent[] RunRefreshMessageTest(User user, boolean runFullRefresh) throws ClassNotFoundException
	{
		RefreshData rData = new RefreshData(user, runFullRefresh);
		
		System.out.println("Running refreshMessage test");
		Socket sendSocket = null;
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = GetSendSocket();
		}
		catch(UnknownHostException e)
		{
			System.out.println("Unknown host: " + e.getMessage());
			return null;
		}
		catch(IOException e)
		{
			System.out.println("IOException: " + e.getMessage());
			return null;
		}
		
		//Serialise the object for transmission
		ObjectOutputStream serialiser = null;
		ObjectInputStream deserialiser = null;
		try
		{
			serialiser = new ObjectOutputStream(sendSocket.getOutputStream());
			deserialiser = new ObjectInputStream(sendSocket.getInputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return null;
		}
		
		try
		{
			MessageType t = MessageType.refreshMessage;
			serialiser.writeObject(t);
			serialiser.writeObject(rData);
			serialiser.flush();
			System.out.println("Data sent");
			PubEvent[] outOfDateEvents = (PubEvent[])deserialiser.readObject();
			
			System.out.println("Retrieved or updated " + outOfDateEvents.length + " events");
			
			for(int i = 0; i < outOfDateEvents.length; ++i)
			{
				System.out.println("Event " + i + ": Is at location: " + outOfDateEvents[i].GetPubLocation().toString());
			}
			
			return outOfDateEvents;
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
			return null;
		}
	}

	private static void RunRespondMessage(int eventId, boolean response)
	{
		ResponseData rData = new ResponseData(CreateHost(), eventId, response);
		
		System.out.println("Running respondMessage test");
		Socket sendSocket = null;
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = GetSendSocket();
		}
		catch(UnknownHostException e)
		{
			System.out.println("Unknown host: " + e.getMessage());
			return;
		}
		catch(IOException e)
		{
			System.out.println("IOException: " + e.getMessage());
			return;
		}
		
		//Serialise the object for transmission
		ObjectOutputStream serialiser = null;
		ObjectInputStream deserialiser = null;
		try
		{
			serialiser = new ObjectOutputStream(sendSocket.getOutputStream());
			deserialiser = new ObjectInputStream(sendSocket.getInputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return;
		}
		
		try
		{
			MessageType t = MessageType.respondMessage;
			serialiser.writeObject(t);
			serialiser.writeObject(rData);
			serialiser.flush();
			System.out.println("Data sent");
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
			return;
		}
	}
	
	private static void RunUpdateMessage(UpdateData newData)
	{
		System.out.println("Running UpdateMessage test");
		Socket sendSocket = null;
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = GetSendSocket();
		}
		catch(UnknownHostException e)
		{
			System.out.println("Unknown host: " + e.getMessage());
			return;
		}
		catch(IOException e)
		{
			System.out.println("IOException: " + e.getMessage());
			return;
		}
		
		//Serialise the object for transmission
		ObjectOutputStream serialiser = null;
		ObjectInputStream deserialiser = null;
		try
		{
			serialiser = new ObjectOutputStream(sendSocket.getOutputStream());
			deserialiser = new ObjectInputStream(sendSocket.getInputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return;
		}
		
		try
		{
			MessageType t = MessageType.updateMessage;
			serialiser.writeObject(t);
			serialiser.writeObject(newData);
			serialiser.flush();
			System.out.println("Data sent");
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
			return;
		}
	}
}

enum TestType
{
	CreateGet,
	CreateUpdate,
	CreateWithGuestGuestCheck,
	CreateWithGuestRespond
}

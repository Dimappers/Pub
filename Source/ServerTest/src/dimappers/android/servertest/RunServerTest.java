package dimappers.android.servertest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import dimappers.android.PubData.AcknoledgementData;
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
	
	private static User localGuest;
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
		//These tests are invalid and for the old server, left for posterity
		// TODO Auto-generated method stub
		SendTestData(CreatePubEvent());
		//SendTestData();
		//ReceiveTestData();
		//RespondTest();
		//ReceiveTestData();
	}
	
	private static User CreateHost()
	{
		return new User("thomas.kiley");
	}
	
	private static PubEvent CreatePubEvent()
	{
		return new PubEvent(new Date(100000), new PubLocation(42, 36, "Spoons Leam"), CreateHost());
	}
	
	private static int SendTestData(PubEvent event) throws ClassNotFoundException
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
			sendSocket = new Socket(InetAddress.getByName("localhost"), 2085);
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
	
	private static void ReceiveTestData()
	{
		System.out.println("Running recieve data tests");
		
		Socket sendSocket = null;
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = new Socket(InetAddress.getByName("localhost"), 2085);
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
		
		ObjectOutputStream serialiser = null;
		try
		{
			serialiser = new ObjectOutputStream(sendSocket.getOutputStream());
			
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return;
		}
		
		try
		{
			MessageType t = MessageType.refreshMessage;
			serialiser.writeObject(t);
			serialiser.writeObject(localGuest);
			serialiser.flush();
			
			ObjectInputStream deserialiser = null;
			
			deserialiser = new ObjectInputStream(sendSocket.getInputStream());
			
			int count = deserialiser.readInt();
			
			Object newPubEvent;
			for(int i = 0; i < count; ++i)
			{
				newPubEvent = deserialiser.readObject();
				PubEvent event = (PubEvent)newPubEvent;
				System.out.println(event.GetPubLocation().pubName);
				//localGuest.AddEvent(event);
			}
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static void RespondTest()
	{
		System.out.println("Running respond tests");
		
		Socket sendSocket = null;
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = new Socket(InetAddress.getByName("localhost"), 2085);
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
		
		ObjectOutputStream serialiser = null;
		try
		{
			serialiser = new ObjectOutputStream(sendSocket.getOutputStream());
			
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return;
		}
		
		try
		{
			//localGuest.DecideOnEvent((PubEvent)localGuest.GetPubEvents().keySet().toArray()[0], PubTripState.Going);
			
			MessageType t = MessageType.respondMessage;
			serialiser.writeObject(t);
			serialiser.writeObject(localGuest);
			serialiser.flush();
			
			/*ObjectInputStream deserialiser = null;
			
			deserialiser = new ObjectInputStream(sendSocket.getInputStream());
			
			Object newPubEvent;
			boolean readingEvents = true;
			while(readingEvents)
			{
				newPubEvent = deserialiser.readObject();
				if(newPubEvent != null)
				{
					PubEvent event = (PubEvent)newPubEvent;
					System.out.println(event.GetPubLocation().pubName);
				}
				else
				{
					readingEvents = false;
				}
			}*/
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
		} 	
	}
}

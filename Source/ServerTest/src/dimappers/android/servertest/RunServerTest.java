package dimappers.android.servertest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Date;

import dimappers.android.PubData.Guest;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.PubTripState;

public class RunServerTest
{

	/**
	 * @param args
	 */
	
	private static Guest localGuest;
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
		//These tests are invalid and for the old server, left for posterity
		// TODO Auto-generated method stub
		SendTestData();
		SendTestData();
		ReceiveTestData();
		RespondTest();
	}
	
	private static void SendTestData() throws ClassNotFoundException
	{
		System.out.println("Running send data tests");
		Socket sendSocket = null;
		
		localGuest = new Guest("thomas.kiley");
		
		//Create some test pub event...
		PubEvent event = new PubEvent(new Date(1000000000), localGuest);
		PubLocation loc = new PubLocation();
		loc.latitudeCoordinate = 42.4;
		loc.longitudeCoordinate = 31.5;
		loc.pubName = "Spoons";
		event.SetPubLocation(loc);
		
		event.AddGuest(localGuest);
		
		//Data before sent
		System.out.println(event.GetStartTime().toString());
		System.out.println(event.GetPubLocation().toString());
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = new Socket(InetAddress.getByName("localhost"), 2084);
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
			MessageType t = MessageType.newPubEventMessage;
			serialiser.writeObject(t);
			serialiser.writeObject(event);
			serialiser.flush();
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
		}
	}
	
	private static void ReceiveTestData()
	{
		System.out.println("Running recieve data tests");
		
		Socket sendSocket = null;
		
		//Create the socket to send through (using port 2084, see in the server file)
		try
		{
			sendSocket = new Socket(InetAddress.getByName("localhost"), 2084);
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
			sendSocket = new Socket(InetAddress.getByName("localhost"), 2084);
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

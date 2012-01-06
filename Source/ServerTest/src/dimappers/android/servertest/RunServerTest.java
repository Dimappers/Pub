package dimappers.android.servertest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.Date;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;

public class RunServerTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
		// TODO Auto-generated method stub
		SendTestData();
	}
	
	private static void SendTestData() throws ClassNotFoundException
	{
		System.out.println("Running send data tests");
		Socket sendSocket = null;
		
		//Create some test pub event...
		PubEvent event = new PubEvent(new Date(1000000000));
		PubLocation loc = new PubLocation();
		loc.latitudeCoordinate = 42.4;
		loc.longitudeCoordinate = 31.5;
		loc.pubName = "Spoons";
		event.SetPubLocation(loc);
		
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
			serialiser.writeObject(event);
			serialiser.flush();
		}
		catch (IOException e)
		{
			System.out.println("Error in serialising the object: " + e.getMessage());
		}
	}
	
	private static void RecieveTestData()
	{
		System.out.println("Running recieve data tests");	
	}

}

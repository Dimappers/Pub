package dimappers.android.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.io.*;

import dimappers.android.PubData.PubEvent;

public class Server {

	/**
	 * @param args
	 */
	
	private static boolean serverRunning = true;
	private static final int PORT = 2084;
	
	public static void main(String[] args) {
		
		System.out.println("Server running on port " + PORT);
		ServerSocket serverSocket = null;
		
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
			//Create the socket to recieve data from upon connection
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
			
			//Desiralise data in to classes - in reality we will have to send some messages before explaining what is coming 
			ObjectInputStream deserialiser = null;
			Object o = null;
			try
			{
				deserialiser = new ObjectInputStream(clientSocket.getInputStream());
				o = deserialiser.readObject();
			} 
			catch (IOException e)
			{
				System.out.println("Error decoding class: " + e.getMessage());
			}
			catch (ClassNotFoundException e)
			{
				System.out.println("Error deserialising class: "+ e.getMessage());
			}
			
			PubEvent outEvent = (PubEvent)o;
		
			//Check data is correct
			System.out.println(outEvent.GetStartTime().toString());
			System.out.println(outEvent.GetPubLocation().toString());
		}
		
		System.out.println("Server closing...");
	}
	
	public static void TerminateServer()
	{
		serverRunning = false;
	}

}

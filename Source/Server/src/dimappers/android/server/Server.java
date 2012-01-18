package dimappers.android.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.io.*;

import dimappers.android.PubData.User;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;

public class Server {

	/**
	 * @param args
	 */
	
	private static boolean serverRunning = true;
	private static final int PORT = 2085;
	
	private static HashMap<String, ServerGuest> guests; 
	
	public static void main(String[] args) {
		EventManager.InitFromScratch(); 
		guests = new HashMap<String, ServerGuest>();
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
					NewEventMessageReceived(deserialiser, serialiser);
					break;
				}
				
				case refreshMessage:
				{
					RefreshMessageReceived(deserialiser, serialiser);
					break;
				}
				
				case respondMessage:
				{
					RespondMessageReceived(deserialiser, serialiser);
					break;
				}
				
				case updateMessage:
				{
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
	private static void NewEventMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream conncetionStreamOut)
	{
		//TODO: 
	}
	
	private static void RefreshMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream conncetionStreamOut)
	{
		//TODO: 
	}
	
	private static void RespondMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream conncetionStreamOut)
	{
		//TODO: 
	}
	
	private static void UpdateMessageReceived(ObjectInputStream connectionStreamIn, ObjectOutputStream conncetionStreamOut)
	{
		//TODO: 
	}

}

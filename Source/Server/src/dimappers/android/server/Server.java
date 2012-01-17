package dimappers.android.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Set;
import java.io.*;

import dimappers.android.PubData.Guest;
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
			MessageType message = null;
			try
			{
				deserialiser = new ObjectInputStream(clientSocket.getInputStream());
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
			case newPubEvent:
			{
				PubEvent outEvent = null;
				try {
					outEvent = (PubEvent)deserialiser.readObject();
				} catch (IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				} catch (ClassNotFoundException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				//Check data is correct
				System.out.println(outEvent.GetStartTime().toString());
				System.out.println(outEvent.GetPubLocation().toString());
				
				for(Guest g : outEvent.GetGuests())
				{
					if(guests.containsKey(g.GetFacebookUserName()))
					{
						guests.get(g.GetFacebookUserName()).AddEvent(outEvent);
					}
					else
					{
						ServerGuest guest = new ServerGuest(g.GetFacebookUserName());
						guest.AddEvent(outEvent);
						guests.put(guest.GetFacebookUserName(), guest);
					}
				}
				break;
			}
				
			case getPubEvent:
			{
				Guest guest = null;
				try {
					guest = (Guest)deserialiser.readObject();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (ClassNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				String guestUserName = guest.GetFacebookUserName();
				if(guests.containsKey(guestUserName))
				{
					//Send some event data back to them
					ObjectOutputStream sendStream = null;
					try {
						sendStream = new ObjectOutputStream(clientSocket.getOutputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					
					Set<PubEvent> events =  guests.get(guestUserName).GetPubEvents().keySet();
					try {
						sendStream.writeInt(events.size());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					for(PubEvent event : events)
					{
						try {
							sendStream.writeObject(event);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					try {
						sendStream.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{	
					//Send back no events
				}
				break;
			}
				
			case respondMessage:
			{
				Guest guest = null;
				try {
					guest = (Guest)deserialiser.readObject();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(guests.containsKey(guest.GetFacebookUserName()))
				{
					ServerGuest serverGuest = guests.get(guest.GetFacebookUserName());
					serverGuest.UpdateEventStatuses(guest.GetPubEvents());
				}
				
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

}

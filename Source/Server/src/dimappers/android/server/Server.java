package dimappers.android.server;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;


public class Server {

	/**
	 * @param args
	 */
	
	public static final boolean IsDebug = true; //Prints out more messages
	
	private static boolean serverRunning = true;
	private static final int PORT = 2085;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		EventManager.InitFromScratch(); 
		System.out.println("Server running on port " + PORT);
		ServerSocket serverSocket = null;
		UserManager.init();
		
		
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
			//Create the socket to receive data from upon connection
			Socket clientSocket = null;
			try
			{
				clientSocket = serverSocket.accept();
			} 
			catch (IOException e)
			{
				System.out.println("Error accepting connection: " + e.getMessage());
			}
			if(IsDebug)
			{
				System.out.println("Data recieved");
			}
			
			new RequestHandlingThread(clientSocket).start();
			clientSocket.close();
		}
	}

}

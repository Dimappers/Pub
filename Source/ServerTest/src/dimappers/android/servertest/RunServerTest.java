package dimappers.android.servertest;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.GoingStatus;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.RefreshResponse;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;

public class RunServerTest
{

	/**
	 * @param args
	 */
	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
		//Checks for all the xml exporters - just wacks them in to the output and then reads them in 
		/* What is all this?
		 * 
		Document xmlDoc;
		{
			Element root = new Element("PubMessage");
			xmlDoc = new Document(root);
			//SAXBuilder builder = new SAXBuilder();
			
			
			root.addContent(CreateHost().writeXmlForTransmission());
			AcknoledgementData ack = new AcknoledgementData(12434);
			root.addContent(ack.writeXml());
			PubLocation loc = new PubLocation(123, 12, "Spoons");
			root.addContent(loc.writeXml());
			
			root.addContent(CreatePubEventWithGuest().writeXml());
			
			RefreshData rd = new RefreshData(new User(5325l), true);
			root.addContent(rd.writeXml());
			
			Calendar freeFrom = Calendar.getInstance();
			freeFrom.set(Calendar.HOUR_OF_DAY, 9);
			ResponseData response = new ResponseData(new User(124l),295, true, freeFrom, "Hello");
			root.addContent(response.writeXml());
			
			UpdateData ud = new UpdateData(234, freeFrom, new PubLocation(12, 12, "Spoons"));
			root.addContent(ud.writeXml());
			
			
			XMLOutputter outputter = new XMLOutputter();
			outputter.output(xmlDoc, System.out);
		}
		System.in.read();
		
		{
			Element root = xmlDoc.getRootElement();
			User user = new User(root.getChild(User.class.getSimpleName()));
			AcknoledgementData ack = new AcknoledgementData(root.getChild(AcknoledgementData.class.getSimpleName()));
			PubLocation loc = new PubLocation(root.getChild(PubLocation.class.getSimpleName()));
			
			PubEvent p = new PubEvent(root.getChild(PubEvent.class.getSimpleName()));
			
			RefreshData rd = new RefreshData(root.getChild(RefreshData.class.getSimpleName()));
			ResponseData response = new ResponseData(root.getChild(ResponseData.class.getSimpleName()));
			UpdateData ud = new UpdateData(root.getChild(UpdateData.class.getSimpleName()));
			
			System.in.read();
		}
		
		*/
		
		if(args.length < 1)
		{
			System.out.println("Error: no test specified, running default");
			args = new String[]{"CreateGet"};
		}
		
		int totalTests = 0;
		int totalTestsPassed = 0;
		
		for(int i = 0; i < args.length; ++i)
		{
			HashMap<String, Boolean> result = RunTest(Enum.valueOf(TestType.class, args[i]));
			int testsPassed = 0;
			for(Entry<String, Boolean> test : result.entrySet())
			{
				if(test.getValue())
				{
					System.out.println("Test: " + test.getKey() + "passed");
					++testsPassed;
				}
				else
				{
					System.out.println("!!Test: " + test.getKey() + "failed");
				}
			}
			
			System.out.println("Test set: " + args[0] + "passed " + testsPassed + "/" + result.size());
			
			totalTests += result.size();
			totalTestsPassed += testsPassed;	
		}
		
		System.out.println("Final result: " + totalTestsPassed + "/" + totalTests);
	}
	
	private static User CreateHost()
	{
		return new User(0l);
	}
	
	private static User CreateGuest()
	{
		return new User(1l);
	}
	
	private static PubEvent CreatePubEvent()
	{
		System.out.println((double)42.0);
		return new PubEvent(Calendar.getInstance(), new PubLocation(42, 36, "Spoons Leam"), CreateHost());
	}
	
	private static PubEvent CreatePubEventWithGuest()
	{
		PubEvent event = new PubEvent(Calendar.getInstance(), new PubLocation(52.29009f, -1.53585f, "Robins Well"), CreateHost());
		event.AddUser(CreateGuest());
		
		return event; 
	}
	
	private static Socket GetSendSocket() throws UnknownHostException, IOException
	{
		return new Socket(InetAddress.getByName(Constants.ServerIp), Constants.Port);
	}
	
	private static HashMap<String, Boolean> RunTest(TestType testType) throws ClassNotFoundException
	{
		HashMap<String, Boolean> tests = new HashMap<String, Boolean>();
		switch(testType)
		{
		case CreateGet:
			{
				int eventId = createPubEventTest(CreatePubEventWithGuest());
				PubEvent[] events = RunRefreshMessageTest(CreateHost(), false);
				PubEvent[] guestEvents = RunRefreshMessageTest(CreateGuest(), false);
				
				//Checks:
				
				{ // Test: NewEventCorrectId
					if(eventId < 0)
					{
						tests.put("NewEventCorrectId", false);
					}
					else
					{
						tests.put("NewEventCorrectId", true);
					}
				}
				
				{ // Test: NewEventEventReturnedToHost
					boolean testPassed = true;
					if(events.length != 0)
					{
						for(int i = 0; i < events.length; ++i)
						{
							if(events[i].GetEventId() == eventId)
							{
								 //Shouldn't return the event to the host
								testPassed = false;
								break;
							}
						}
					}
					tests.put("NewEventEventReturnedToHost", testPassed);
				}
				
				{ // Test: NewEventEventReturnedToGuest
					boolean eventFound = false;
					for(int i = 0; i < guestEvents.length; ++i)
					{
						if(guestEvents[i].GetEventId() == eventId)
						{
							eventFound = true;
						}
					}
					
					tests.put("NewEventEventReturnedToGuest", eventFound);
				}
				
				break;
			}
			
		case CreateUpdate:
			{
				int eventId = createPubEventTest(CreatePubEventWithGuest());
				PubEvent[] events = RunRefreshMessageTest(CreateHost(), false);
				PubEvent[] guestEvents = RunRefreshMessageTest(CreateGuest(), false);
				
				PubLocation updateLocation = new PubLocation(0,0, "Satchwells");
				UpdateData myTestUpdate = new UpdateData(eventId, null, updateLocation);
				RunUpdateMessage(myTestUpdate);
				
				PubEvent[] eventsAfterUpdate = RunRefreshMessageTest(CreateHost(), false);
				PubEvent[] guestEventsAfterUpdate = RunRefreshMessageTest(CreateGuest(), false);
				
				{ // Test: NewEventCorrectId
					if(eventId < 0)
					{
						tests.put("NewEventCorrectId", false);
					}
					else
					{
						tests.put("NewEventCorrectId", true);
					}
				}
				
				{ // Test: NewEventEventReturnedToHost
					boolean testPassed = true;
					if(events.length != 0)
					{
						for(int i = 0; i < events.length; ++i)
						{
							if(events[i].GetEventId() == eventId)
							{
								 //Shouldn't return the event to the host
								testPassed = false;
								break;
							}
						}
					}
					tests.put("NewEventEventReturnedToHost", testPassed);
				}
				
				{ //Test: EventEventReturnedToGuest
					boolean eventFound = false;
					for(int i = 0; i < guestEvents.length; ++i)
					{
						if(guestEvents[i].GetEventId() == eventId)
						{
							eventFound = true;
							break;
						}
					}
					
					tests.put("EventEventReturnedToGuest", eventFound);
				}
				
				{ // Test: UpdateReturnedToHost
					boolean testPassed = true;
					for(int i = 0; i < eventsAfterUpdate.length; ++i)
					{
						if(eventsAfterUpdate[i].GetEventId() == eventId)
						{
							 //Shouldn't return the event to the host
							testPassed = false;
							break;
						}
					}
					
					tests.put("UpdateReturnedToHost", testPassed);
				}
				
				{ // Test: UpdateReturnedToGuest & UpdateWasApplied
					boolean eventFound = false;
					boolean wasUpdated = false;
					for(int i = 0; i < guestEventsAfterUpdate.length; ++i)
					{
						if(guestEventsAfterUpdate[i].GetEventId() == eventId)
						{
							eventFound = true;
							if(guestEventsAfterUpdate[i].GetPubLocation().equals(updateLocation))
							{
								wasUpdated = true;
							}
							break;
						}
					}
					
					tests.put("UpdateReturnedToGuest", eventFound);
					tests.put("UpdateWasApplied", wasUpdated);
				}
				
				break;
			}
			
		case CreateWithGuestRespond:
			{
				int eventId = createPubEventTest(CreatePubEventWithGuest());
				
				PubEvent[] guestEvents = RunRefreshMessageTest(CreateGuest(), false);
				
				for(PubEvent event : guestEvents)
				{
					RunRespondMessage(event.GetEventId(), true);
				}
				
				PubEvent[] hostEventsAfterRespond = RunRefreshMessageTest(CreateHost(), false);
				PubEvent[] guestEventsAfterRespond = RunRefreshMessageTest(CreateGuest(), false);
				
				
				{ // Test: NewEventCorrectId
					if(eventId < 0)
					{
						tests.put("NewEventCorrectId", false);
					}
					else
					{
						tests.put("NewEventCorrectId", true);
					}
				}
				
				{ //Test: EventReturnedToGuest
					boolean eventFound = false;
					for(int i = 0; i < guestEvents.length; ++i)
					{
						if(guestEvents[i].GetEventId() == eventId)
						{
							eventFound = true;
							break;
						}
					}
					
					tests.put("EventReturnedToGuest", eventFound);
				}
				
				{ //Test: EventReturnedToGuestAfterRespond
					boolean testPassed = true;
					for(int i = 0; i < guestEventsAfterRespond.length; ++i)
					{
						if(guestEventsAfterRespond[i].GetEventId() == eventId)
						{
							testPassed = false;
							break;
						}
					}
					
					tests.put("EventReturnedToGuestAfterRespond", testPassed);
				}
				
				{ //Test: EventReturnedToHostAfterRespond  & EventResponseGivenToHost
					boolean eventFound = false;
					boolean userUpdated = false;
					for(int i = 0; i < hostEventsAfterRespond.length; ++i)
					{
						if(hostEventsAfterRespond[i].GetEventId() == eventId)
						{
							eventFound = true;
							
							userUpdated = hostEventsAfterRespond[i].GetGoingStatusMap().get(CreateGuest()).goingStatus == GoingStatus.going;
							
							break;
						}
					}
					
					tests.put("EventReturnedToHostAfterRespond", eventFound);
					tests.put("EventResponseGivenToHost", userUpdated);
				}
				
				break;
			}
		}
		
		return tests;
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
		XMLOutputter outputter = new XMLOutputter();
		Element root = new Element("Message");
		Document doc = new Document(root);
		
		Element messageType = new Element("MessageType");
		messageType.addContent(MessageType.newPubEventMessage.toString());
		root.addContent(messageType);
		root.addContent(event.writeXml());
		try
		{
			System.out.println("Test1");
			OutputStream outStream = sendSocket.getOutputStream();
			outputter.output(doc, outStream);
			outStream.flush();
			System.out.println("Test2");
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return -3;
		}
		
		SAXBuilder xmlBuilder = new SAXBuilder();
		Document returnDoc;
		
		try {
			InputStream inStream = sendSocket.getInputStream();
			int a = inStream.read();
			while(a != -1)
			{
				System.out.println((char)a);
				a = inStream.read();
			}
			returnDoc = xmlBuilder.build(inStream);
		} catch (IOException e) {
			System.out.println("Error in building the acknowledgement");
			return -4;
		} catch (JDOMException e) {
			System.out.println("JDOM parsing error (balls)");
			return -4;
		}
		
		AcknoledgementData globalEventId = new AcknoledgementData(returnDoc.getRootElement().getChild(AcknoledgementData.class.getSimpleName()));
		return globalEventId.globalEventId;
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
		XMLOutputter outputter = new XMLOutputter();
		
		Element root = new Element("Message");
		Element messageType = new Element("MessageType");
		messageType.addContent(MessageType.refreshMessage.toString());
		
		Document doc = new Document(root);
		root.addContent(rData.writeXml());
		root.addContent(messageType);
		
		try
		{
			outputter.output(doc, sendSocket.getOutputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return null;
		}
		
		SAXBuilder xmlBuilder = new SAXBuilder();
		Document returnDoc;
		
		try {
			returnDoc = xmlBuilder.build(sendSocket.getInputStream());
			RefreshResponse response = new RefreshResponse(returnDoc.getRootElement().getChild(RefreshResponse.class.getSimpleName()));
			PubEvent[] outOfDateEvents = response.getEvents();
			
			for(int i = 0; i < outOfDateEvents.length; ++i)
			{
				System.out.println("Event " + i + ": Is at location: " + outOfDateEvents[i].GetPubLocation().toString());
			}
			
			return outOfDateEvents;
		} catch (Exception e) {
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
		XMLOutputter outputter = new XMLOutputter();
		Element root = new Element("Message");
		Document doc = new Document(root);
		
		Element messageType = new Element("MessageType");
		messageType.addContent(MessageType.respondMessage.toString());
		
		root.addContent(messageType);
		root.addContent(rData.writeXml());
		
		try
		{
			outputter.output(doc, sendSocket.getOutputStream());
			System.out.println("Data sent");
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
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
		XMLOutputter outputter = new XMLOutputter();
		Element root = new Element("Message");
		Document doc = new Document(root);
		
		Element messageType = new Element("MessageType");
		messageType.addContent(MessageType.updateMessage.toString());
		root.addContent(messageType);
		root.addContent(newData.writeXml());
		
		try
		{
			outputter.output(doc, sendSocket.getOutputStream());
			System.out.println("Data sent");
		}
		catch (IOException e)
		{
			System.out.println("Error in creating serialser: " + e.getMessage());
			return;
		}
		
	}
}

enum TestType
{
	CreateGet,
	CreateUpdate,
	CreateWithGuestRespond
}

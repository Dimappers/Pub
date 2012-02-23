package dimappers.android.pub;

import java.io.IOException;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import android.os.AsyncTask;
import android.util.Log;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.MessageType;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;

public class DataSender {
	
	private Queue<Document> queue;
	private SenderThread senderThread;
	private PubService service;
	
	DataSender(PubService service){
		queue = new ArrayBlockingQueue<Document>(100);
		senderThread = new SenderThread();
		senderThread.execute(new Object[]{});
		this.service = service;
	}
	
	public void sendEvent(PubEvent event) {
		
		Element root = new Element("Message");
		Document xmlDoc = new Document(root);
		
		
		Element messageTypeElement = new Element("MessageType");
		messageTypeElement.addContent(MessageType.newPubEventMessage.toString());
		root.addContent(messageTypeElement);
		root.addContent(event.writeXml());
		addToSendQueue(xmlDoc);
	}
	
	public void requestUpdate(User u) throws IOException { updates(u,false); }
	
	public void requestAllUpdates(User u) throws IOException { updates(u,true); }
	
	private void updates(User u, boolean full) throws IOException {
	
		Element root = new Element("RequestUpdate");
		Document xmlDoc = new Document(root);
		
		RefreshData rd = new RefreshData(u, full);
		root.addContent(rd.writeXml());
		
		addToSendQueue(xmlDoc);
	}
	
	public void sendResponse(User u, int event, boolean going, Calendar freefrom, String msg) throws IOException {
	
		Element root = new Element("RequestUpdate");
		Document xmlDoc = new Document(root);

		ResponseData response = new ResponseData(u,event,going,freefrom,msg);
		root.addContent(response.writeXml());
		
		addToSendQueue(xmlDoc);

	}
	
	public void sendEventUpdates(int event, Calendar start, PubLocation pub) throws IOException {
		
		Element root = new Element("UpdateEvent");
		Document xmlDoc = new Document(root);
		
		UpdateData ud = new UpdateData(event, start, pub);
		root.addContent(ud.writeXml());
		
		addToSendQueue(xmlDoc);
		
	}
	
	private void addToSendQueue(Document xmlDoc)
	{
		queue.add(xmlDoc);
	}
	
	class SenderThread extends AsyncTask<Object, Object, Object>
	{
		boolean running = true;
		@Override
		protected Object doInBackground(Object... params) {
			while(running || DataSender.this.queue.size() > 0 )
			{
				Document xmlDoc;
				try {
					xmlDoc = ((ArrayBlockingQueue<Document>) DataSender.this.queue).take();
				} catch (InterruptedException e1) {
					Log.d(Constants.MsgError, "Sender thread was interupted");
					continue;
				}
				
				XMLOutputter outputter = new XMLOutputter();
				try {
					outputter.output(xmlDoc, System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Receive id back from host
				service.getDataStore().notifySentEventHasId((int)(Math.random() * 1000));
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(queue.size() > 0)
				Log.d(Constants.MsgWarning, "Sender thread killed with " + queue.size() + " document(s) left to send");
		}
		
		//Stops the thread as soon as all documents have been sent
		public void stop()
		{
			running = false;
		}
	}
}

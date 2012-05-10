package dimappers.android.pub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.os.AsyncTask;
import android.util.Log;

import dimappers.android.PubData.Constants;

public class DataSender {
	
	private static final String endString = "</Message>";
	
	private Queue<Request<?, ?>> queue;
	private SenderThread senderThread;
	
	DataSender(){
		queue = new ArrayBlockingQueue<Request<?, ?>>(500);
		senderThread = new SenderThread();
		senderThread.execute(new Object[]{});
	}
	
	public <K, T> void addRequest(IDataRequest<K, T> request, final IRequestListener<T> listener, HashMap<K, T> store)
	{
		Request<K, T> r = new Request<K, T>(request, listener, store);
		queue.add(r);
	}
	
	class Request<K, T>
	{
		public IDataRequest<K, T> request;
		public final IRequestListener<T> listener;
		public HashMap<K, T> store;
		
		public Request(IDataRequest<K, T> request, final IRequestListener<T> listener, HashMap<K, T> store)
		{
			this.request =request;
			this.listener = listener;
			this.store = store;
		}
		
		public void performRequest()
		{
			request.performRequest(listener, store);
		}
	}
	
	class SenderThread extends AsyncTask<Object, Object, Object>
	{
		boolean running = true;
		@Override
		protected Object doInBackground(Object... params) {
			while(running || DataSender.this.queue.size() > 0 )
			{				
				Request<?, ?> r;
				try {
					r  = ((ArrayBlockingQueue<Request<?, ?>>)DataSender.this.queue).take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.d(Constants.MsgError, "Queue interrupted");
					continue;
				}
				
				r.performRequest();	
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
	
	public static Socket sendDocument(Document docToSend) throws UnknownHostException, IOException
	{
		Socket sendSocket =  new Socket(InetAddress.getByName(Constants.ServerIp), Constants.Port);
		XMLOutputter outputter = new XMLOutputter();
		OutputStream outStream = sendSocket.getOutputStream();
		outputter.output(docToSend, outStream);
		outStream.flush();
		
		return sendSocket;
	}
	
	public static Document readTillEndOfMessage(InputStream inStream) throws IOException, JDOMException
	{
		SAXBuilder docBuilder = new SAXBuilder();
		int nextByte = inStream.read();
		StringBuilder sBuilder = new StringBuilder();
		while(nextByte != -1)
		{
			sBuilder.append((char)nextByte);
			if(sBuilder.length() >= endString.length() && sBuilder.toString().endsWith(endString))
			{
				break;
			}
			else
			{
				nextByte = inStream.read();
			}
		}
		System.out.println(sBuilder.toString());
		StringReader reader = new StringReader(sBuilder.toString());
		return docBuilder.build(reader);
	}
	
	public static Document sendReceiveDocument(Document docToSend) throws UnknownHostException, IOException, JDOMException
	{
		Socket s = sendDocument(docToSend);
		
		return readTillEndOfMessage(s.getInputStream());
	}
}

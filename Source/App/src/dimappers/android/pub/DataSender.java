package dimappers.android.pub;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
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
	
	private Queue<Request<?, ?>> queue;
	private SenderThread senderThread;
	
	DataSender(){
		queue = new ArrayBlockingQueue<Request<?, ?>>(100);
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
}

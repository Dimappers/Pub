package dimappers.android.pub;

import java.util.Timer;
import java.util.TimerTask;

public class DataReceiver
{
	private IPubService service;
	private PerformRefresh refresher;
	private final long UpdateFrequency = 900000; //Check every 15 minutes
	
	
	public DataReceiver(IPubService service)
	{
		this.service = service;
		Timer updateScheduler = new Timer();
		refresher = new PerformRefresh();
		refresher.doUpdate(true); //when we start the receiver do a full update to get back up to speed
		updateScheduler.schedule(refresher, UpdateFrequency, UpdateFrequency);
	}
	
	public void forceUpdate(boolean fullUpdate)
	{
		refresher.doUpdate(fullUpdate);
	}
	
	
	class PerformRefresh extends TimerTask
	{
		
		public void run()
		{
			doUpdate(false);
		}
		
		//TODO: Test me once we can get xmls from the server
		public void doUpdate(boolean fullUpdate)
		{
			DataRequestRefresh refresh = new DataRequestRefresh(fullUpdate);
			DataReceiver.this.service.addDataRequest(refresh, new IRequestListener<PubEventArray>() {


				
				public void onRequestComplete(PubEventArray data) {
				}


				
				public void onRequestFail(Exception e) {
					//TODO: Don't know?					
				}
				
			});
		}
		
	}
}

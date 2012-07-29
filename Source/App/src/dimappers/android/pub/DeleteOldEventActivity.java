package dimappers.android.pub;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class DeleteOldEventActivity extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		IPubService service = (IPubService) peekService(context, new Intent(context, PubService.class));
		
		if(service!=null)
		{
			int eventId = intent.getIntExtra(Constants.CurrentWorkingEvent, Integer.MIN_VALUE);
			PubEvent pub = service.getEvent(eventId);
			if(pub!=null)
			{
				service.DeleteEvent(pub);
			}
		}
		else
		{
			AssociatedPendingIntents.rescheduleBroadcast(context, intent);
		}
	}

}

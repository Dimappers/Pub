package dimappers.android.pub;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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

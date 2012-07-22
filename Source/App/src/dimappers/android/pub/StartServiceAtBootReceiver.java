package dimappers.android.pub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartServiceAtBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		
		Intent startServiceIntent = new Intent(context, PubService.class);
        context.startService(startServiceIntent);

	}

}

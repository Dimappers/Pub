package dimappers.android.pub;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LaunchApplication extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	int facebookId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	//need to log into Facebook if not logged in before
    	facebookId = 1238;
    	
    	setContentView(R.layout.main);
    	
    	Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setOnClickListener(this);
    	
    	Button button_invites = (Button)findViewById(R.id.invites_button);
    	button_invites.setOnClickListener(this);
    	
    	AppUser user = new AppUser(facebookId);
    	
    	Toast.makeText(getApplicationContext(), "User id: " + user.getUserId().toString(), 200).show();
    }
    public void onClick(View v)
    {
    	Intent i;
		
		switch (v.getId()) {
		case R.id.organise_button : 
		{
			Bundle b = new Bundle();
			b.putInt("facebookId", facebookId);
			i = new Intent(this, Pending.class);
			i.putExtras(b);
			startActivityForResult(i,1);
			break;
		}
		case R.id.invites_button : {
			
			i = new Intent(this, Events.class);
			startActivity(i);
			break;
		}
		}
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode==RESULT_OK)
    	{
		 if(requestCode==0)
		 {
			 super.onActivityResult(requestCode, resultCode, data);
			 Intent i = new Intent(this, Events.class);	
			 startActivity(i);
		 }
		 if(requestCode==1)
		 {
			super.onActivityResult(requestCode, resultCode, data);
			Intent i = new Intent(this,Organise.class);
			i.putExtras(data.getExtras());
			startActivityForResult(i,0);
		 }
    	}
	 }
}
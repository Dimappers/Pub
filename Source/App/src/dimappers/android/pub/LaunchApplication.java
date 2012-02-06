package dimappers.android.pub;

import dimappers.android.PubData.AcknoledgementData;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubLocation;
import dimappers.android.PubData.RefreshData;
import dimappers.android.PubData.ResponseData;
import dimappers.android.PubData.UpdateData;
import dimappers.android.PubData.User;
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
	AppUser facebookUser;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	//need to log into Facebook if not logged in before
    	facebookUser = GetFacebookUser();
    	
    	setContentView(R.layout.main);
    	
    	Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setOnClickListener(this);
    	
    	Button button_invites = (Button)findViewById(R.id.invites_button);
    	button_invites.setOnClickListener(this);
    	
    	AppUser user = new AppUser(facebookId);
    	//Toast.makeText(getApplicationContext(), "User id: " + user.getUserId().toString(), 200).show();
    }
    public void onClick(View v)
    {
    	Intent i;
		Bundle b = new Bundle();
		b.putSerializable(Constants.CurrentFacebookUser, facebookUser);
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
			i.putExtras(b);
			startActivity(i);
			break;
		}
		}
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode==RESULT_OK)
    	{
		 if(requestCode == Constants.FromOrganise)
		 {
			 super.onActivityResult(requestCode, resultCode, data);
			 Intent i = new Intent(this, Events.class);	
			 startActivity(i);
		 }
		 if(requestCode==Constants.FromPending)
		 {
			super.onActivityResult(requestCode, resultCode, data);
			Intent i = new Intent(this,Organise.class);
			i.putExtras(data.getExtras());
			startActivityForResult(i, Constants.FromOrganise);
		 }
    	}
	 }
    
    private AppUser GetFacebookUser()
    {
    	//Get the facebook id - from login details possibly also authentication stuff
    	return new AppUser(14);
    }
}
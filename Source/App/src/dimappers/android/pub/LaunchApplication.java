package dimappers.android.pub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import net.awl.appgarden.sdk.AppGardenAgent;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import dimappers.android.PubData.Constants;

public class LaunchApplication extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	AppUser facebookUser;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
      	if(!isNetworkAvailable()) {
       		Intent i = new Intent(this, NoInternet.class); 
       		startActivityForResult(i,Constants.NoInternet);
       	}
    	//TODO: need to log into Facebook here if not logged in before
    	facebookUser = GetFacebookUser();
    	
        if(getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_90||getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_270)
        {
      	  setContentView(R.layout.main_hor);
        }
        else {
      	  setContentView(R.layout.main);
        }
        
    	AppGardenAgent.startSchoolYear(this, "e8428bc2-8ce9-4dec-b5c3-20b5e42738c9");
    	
    	Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setOnClickListener(this);
    	
    	Button button_invites = (Button)findViewById(R.id.invites_button);
    	button_invites.setOnClickListener(this);
    	
    	Intent startServiceIntent = new Intent(this, PubService.class);
    	startService(startServiceIntent);
    }
    
    public void onClick(View v)
    {
    	Intent i;
    	Bundle b = new Bundle();
    	b.putSerializable(Constants.CurrentFacebookUser, facebookUser);
    	switch (v.getId()) {
    		case R.id.organise_button : 
    		{
    			i = new Intent(this, Pending.class);
    			i.putExtras(b);
    			startActivityForResult(i,Constants.FromPending);
    			break;
    		}
    		case R.id.invites_button : {

    			i = new Intent(this, CurrentEvents.class);
    			i.putExtras(b);
    			startActivity(i);
    			break;
    		}
    	}
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
    	AppGardenAgent.onActivityResult(requestCode, resultCode, data);
    	if(resultCode==RESULT_OK)
    	{
    		if(requestCode == Constants.FromOrganise)
    		{
<<<<<<< HEAD

    			Intent i = new Intent(this, CurrentEvents.class);	
=======
    			Intent i = new Intent(this, Events.class);	
>>>>>>> 285ffae13f75020fbf5fac692aed6426171dbf8b
    			i.putExtras(data.getExtras());
    			startActivity(i);
    		}
    		else if(requestCode==Constants.FromPending)
    		{
    			Intent i = new Intent(this, Organise.class);
    			i.putExtras(data.getExtras());
    			startActivityForResult(i, Constants.FromOrganise);
    		}
    		else if(requestCode==Constants.NoInternet)
    		{
    			//FIXME: application won't launch if no internet
    			if(!isNetworkAvailable()){finish();}
    		}
    	}
    }
    @Override
    public void onDestroy() {
    	super.onDestroy();
        AppGardenAgent.summerBreak();
    }
    
    private boolean isNetworkAvailable() {
        return ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo()!=null;
    }
    
    private AppUser GetFacebookUser()
    {
    	//Get the facebook id - from login details possibly also authentication stuff
    	return new AppUser(14);
    }
}
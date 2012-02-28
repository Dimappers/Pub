package dimappers.android.pub;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;


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
import android.widget.TextView;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.User;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;
import com.facebook.android.AsyncFacebookRunner.*;


public class LaunchApplication extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	AppUser facebookUser;
	
	Facebook facebook = new Facebook(Constants.FacebookAppId);
	AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
	String FILENAME = "AndroidSSO_data";
	private SharedPreferences mPrefs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
      	
    	//Check for internet
    	if(!isNetworkAvailable()) {
       		Intent i = new Intent(this, NoInternet.class); 
       		startActivityForResult(i,Constants.NoInternet);
       	}
    	
    	//Orientate the screen
        if(getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_90||getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_270)
        {
      	  setContentView(R.layout.main_hor);
        }
        else {
      	  setContentView(R.layout.main);
        }
        
        
    	AppGardenAgent.startSchoolYear(this, "e8428bc2-8ce9-4dec-b5c3-20b5e42738c9");
    	
    	if(!Constants.emulator)
    	{
	    	/* Get existing access_token if any */
	    	mPrefs = getPreferences(MODE_PRIVATE);
	        String access_token = mPrefs.getString("access_token", null);
	        long expires = mPrefs.getLong("access_expires", 0);
	        if(access_token != null) {
	        	Log.d(Constants.MsgInfo, "Facebook token found: " + access_token);
	            facebook.setAccessToken(access_token);
	        }
	        if(expires != 0) {
	        	Log.d(Constants.MsgInfo, "Expirery date loaded: " + expires);
	            facebook.setAccessExpires(expires);
	        }
	        /* Only call authorize if the access_token has expired */
	        if(!facebook.isSessionValid()) {  	
	        	facebook.authorize(this, new String[] { "email", "publish_checkins", "user_location", "friends_location" }, Constants.FromFacebookLogin, new DialogListener() {
	        		public void onComplete(Bundle values) {
	        			SharedPreferences.Editor editor = mPrefs.edit();
	                    editor.putString("access_token", facebook.getAccessToken());
	                    editor.putLong("access_expires", facebook.getAccessExpires());
	                    editor.commit();
	                    
	        		}
	
	        		public void onFacebookError(FacebookError error) {}
	        		
	        		public void onError(DialogError e) {}
	
	        		public void onCancel() {}
	        	});
	        	
	        }
	        else 
	        {
	        	getPerson();
	        }
    	}
    	else
    	{
    		facebookUser = new AppUser(12387L, "Made Up");
    		//Don't start the service until we are logged in to facebook
        	Intent startServiceIntent = new Intent(this, PubService.class);
        	Bundle b = new Bundle();
        	b.putSerializable(Constants.CurrentFacebookUser, facebookUser);
        	startServiceIntent.putExtras(b);
        	startService(startServiceIntent);
    	}
    	Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setOnClickListener(this);
    	
    	Button button_invites = (Button)findViewById(R.id.invites_button);
    	button_invites.setOnClickListener(this);
    
    }
    
    private void getPerson()
    {
    	JSONObject me;
		try {
			Log.d(Constants.MsgInfo, "Getting information about current user");
			me = new JSONObject(facebook.request("me"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "Malformed url when requesting info about current facebook user: " + e.getMessage());
			finish();
			return;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "Jason: " + e.getMessage());
			finish();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "IO when retrieving current user: " + e.getMessage());
			finish();
			return;
		}
    	String id = null;
    	String name = null;
		try {
			Log.d(Constants.MsgInfo, "Got info about person: " + me.toString(4));
			id = me.getString("id");
			name = me.getString("name");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d(Constants.MsgError, "Jason error in reading info about current user: " + e.getMessage());
		}
    	
		Log.d(Constants.MsgInfo, "Logged in as user: " + name + " with ID: " + id);
		
    	facebookUser = new AppUser(Long.parseLong(id));
    	
    	//Don't start the service until we are logged in to facebook
    	Intent startServiceIntent = new Intent(this, PubService.class);
    	Bundle b = new Bundle();
    	b.putSerializable(Constants.CurrentFacebookUser, facebookUser);
    	b.putString(Constants.AuthToken, facebook.getAccessToken());
    	b.putLong(Constants.Expires, facebook.getAccessExpires());
    	
    	startServiceIntent.putExtras(b);
    	startService(startServiceIntent);
    	
    	
    }
    
    public void onClick(View v)
    {
    	Intent i;
    	Bundle b = new Bundle();
    	b.putSerializable(Constants.CurrentFacebookUser, facebookUser);
    	b.putString(Constants.AuthToken, facebook.getAccessToken());
    	b.putLong(Constants.Expires, facebook.getAccessExpires());
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
    		
    		case R.id.logoutfb_button : {
    			
    			mAsyncRunner.logout(getBaseContext(), new RequestListener() {
    				  public void onComplete(String response, Object state) {}
    				  
    				  public void onIOException(IOException e, Object state) {}
    				  
    				  public void onFileNotFoundException(FileNotFoundException e,
    				        Object state) {}
    				  
    				  public void onMalformedURLException(MalformedURLException e,
    				        Object state) {}
    				  
    				  public void onFacebookError(FacebookError e, Object state) {}
    				});
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
    			Intent i = new Intent(this, CurrentEvents.class);	
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
    			if(!isNetworkAvailable()){finish();}
    		}
    		else if(requestCode == Constants.FromFacebookLogin)
    		{
    			facebook.authorizeCallback(requestCode, resultCode, data);
            	getPerson();
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
    
}
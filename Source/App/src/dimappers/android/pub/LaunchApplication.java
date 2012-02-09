package dimappers.android.pub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
    	//need to log into Facebook if not logged in before
    	facebookUser = GetFacebookUser();
    	
    	setContentView(R.layout.main);
    	
    	Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setOnClickListener(this);
    	
    	Button button_invites = (Button)findViewById(R.id.invites_button);
    	button_invites.setOnClickListener(this);
    	//Toast.makeText(getApplicationContext(), "User id: " + user.getUserId().toString(), 200).show();
    	
    	SharedPreferences dataStore = getPreferences(MODE_PRIVATE);
    	String encodedLoadedData = dataStore.getString(Constants.SaveDataName, "NoneLoaded");
    	if(encodedLoadedData == "NoneLoaded")
    	{
    		//first time the app has been run
    		StoredData.Init(null, dataStore.edit());
    	}
    	else
    	{
    		byte [] data = Base64.decode( encodedLoadedData, Base64.DEFAULT );
    		ObjectInputStream objectReader;
    		try
    		{
    			objectReader = new ObjectInputStream(new ByteArrayInputStream(data));
    			StoredData storedData = (StoredData)objectReader.readObject();
    			StoredData.Init(storedData, dataStore.edit());
    			objectReader.close();
    		} catch (StreamCorruptedException e)
    		{
    			Log.d(Constants.MsgError, "Error reading input file - StreamCorrupted");
    			finish();
    		} catch (IOException e)
    		{
    			Log.d(Constants.MsgError, "Error reading input file - IOException");
    			finish();
    		} catch (ClassNotFoundException e)
    		{
    			Log.d(Constants.MsgError, "Error casting input file");
    			finish();
    		}
    	}
    	
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
    			i.putExtras(data.getExtras());
    			startActivity(i);
    		}
    		else if(requestCode==Constants.FromPending)
    		{
    			super.onActivityResult(requestCode, resultCode, data);
    			Intent i = new Intent(this, Organise.class);
    			i.putExtras(data.getExtras());
    			startActivityForResult(i, Constants.FromOrganise);
    		}
    		else if(requestCode==Constants.NoInternet)
    		{
    			super.onActivityResult(requestCode, resultCode, data);
    			finish();
    		}
    	}
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
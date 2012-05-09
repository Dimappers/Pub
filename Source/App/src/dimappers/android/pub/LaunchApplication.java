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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
	
	IPubService service;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	
    	if(getIntent().getExtras() != null)
    	{
	    	if(getIntent().getExtras().containsKey(Constants.CurrentWorkingEvent))
	    	{
	    		Intent i = new Intent(this, CurrentEvents.class);	
				//i.putExtras(getIntent().getExtras());
	    		Bundle b = new Bundle();
	    		b.putInt(Constants.CurrentWorkingEvent, getIntent().getExtras().getInt(Constants.CurrentWorkingEvent));
	    		i.putExtras(b);
				startActivity(i);
	    	}
    	}
    	
    	//Check for internet
    	if(!isNetworkAvailable()) {
       		Intent i = new Intent(this, NoInternet.class); 
       		startActivityForResult(i,Constants.NoInternet);
       	}
    	else {
    	//Orientate the screen - currently just using standard rotation for everything
        /*if(getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_90||getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_270)
        {
      	  setContentView(R.layout.main_hor);
        }
        else {*/
      	  setContentView(R.layout.main);
        //}
        
        
    	AppGardenAgent.startSchoolYear(this, "3c7b17c9-5ee0-4b3e-8edb-94a5ccaa7fe2");
    	
    	authoriseFacebook();
    	
    	Typeface font = Typeface.createFromAsset(getAssets(), "SkratchedUpOne.ttf"); 
    	
    	Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setVisibility(View.GONE);
    	button_organise.setOnClickListener(this);
    	button_organise.setTypeface(font);
    	
    	findViewById(R.id.progressbar).setVisibility(View.GONE);
    	
    	Button button_invites = (Button)findViewById(R.id.invites_button);
    	button_invites.setOnClickListener(this);
    	button_invites.setVisibility(View.GONE);
    	button_invites.setTypeface(font);
    	    
    	TextView pub_title = (TextView)findViewById(R.id.main_pub_title);
    	pub_title.setTypeface(font);
    	//pub_title.setVisibility(View.GONE);
    	
    	findViewById(android.R.id.list).setVisibility(View.GONE); }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
    	return super.onCreateOptionsMenu(menu);
    	/*
		MenuItem edit = menu.add(0, Menu.NONE, 0, "Log out of Facebook");
		edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				new AlertDialog.Builder(LaunchApplication.this).setMessage("Are you sure you want to log out of Facebook?")  
				.setTitle("Alert")  
				.setCancelable(true)  
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mAsyncRunner.logout(getBaseContext(), new RequestListener() {
		    				  public void onComplete(String response, Object state) {
		    					Editor e = mPrefs.edit();
		    					e.remove("access_token");
		    					e.remove("access_expires");
		    					e.commit();
		    					authoriseFacebook();
		    				  }
		    				  
		    				  public void onIOException(IOException e, Object state) {}
		    				  
		    				  public void onFileNotFoundException(FileNotFoundException e,
		    				        Object state) {}
		    				  
		    				  public void onMalformedURLException(MalformedURLException e,
		    				        Object state) {}
		    				  
		    				  public void onFacebookError(FacebookError e, Object state) {}
		    				});
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.show(); 
				
				return true;
			}
		});
		return super.onCreateOptionsMenu(menu);*/
	}
    
    private void authoriseFacebook()
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
		/* Only call authorise if the access_token has expired */
		if(!facebook.isSessionValid()) {
			Log.d(Constants.MsgInfo, "No valid token found - authorising with Facebook");
			facebook.authorize(this, new String[] { "email", "user_location", "friends_location", "user_photos", "read_stream" }, Constants.FromFacebookLogin, new DialogListener() {
				public void onComplete(Bundle values) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token", facebook.getAccessToken());
					editor.putLong("access_expires", facebook.getAccessExpires());
					editor.commit();
					//Should getPerson() be called here?
					runOnUiThread(new ShowButtonsHideProgBar());
				}

				public void onFacebookError(FacebookError error) { Log.d(Constants.MsgError, "Error authenticating Facebook: " + error.getMessage());}

				public void onError(DialogError e) { Log.d(Constants.MsgError, "Error from dialog: " + e.getMessage()); }

				public void onCancel() {}
			});

		}
        else 
        {
        	getPerson();
        }	
    }
    
    private void getPerson()
    {
    	Object[] array = new Object[2];
    	array[0] = new IRequestListener<AppUser>(){

			public void onRequestComplete(AppUser appUser) {
		    	
				facebookUser = appUser;
		    	//Don't start the service until we are logged in to facebook
		    	Intent startServiceIntent = new Intent(LaunchApplication.this, PubService.class);
		    	Bundle b = new Bundle();
		    	b.putSerializable(Constants.CurrentFacebookUser, facebookUser);
		    	b.putString(Constants.AuthToken, facebook.getAccessToken());
		    	b.putLong(Constants.Expires, facebook.getAccessExpires());
		    	
		    	startServiceIntent.putExtras(b);
		    	startService(startServiceIntent);
		    	
				bindService(new Intent(LaunchApplication.this, PubService.class), connection, 0);
		    	
		    	runOnUiThread(new ShowButtonsHideProgBar());
			}

			public void onRequestFail(Exception e) {
				new AlertDialog.Builder(LaunchApplication.this)
						.setCancelable(true)  
						.setPositiveButton("Exit", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,	int which) 
							{
								dialog.cancel();
								LaunchApplication.this.finish();
							}
						})
						.setTitle("Error")
						.setMessage("An error has occurred trying to authenticate with Facebook")
						.show();
			}};
	    array[1] = facebook;
    	new GetPerson().execute(array);
    }
    
    class ShowButtonsHideProgBar implements Runnable 
    {
		public void run() {
			//findViewById(R.id.progressbar).setVisibility(View.GONE);
	    	findViewById(R.id.organise_button).setVisibility(View.VISIBLE);
	    	findViewById(R.id.invites_button).setVisibility(View.VISIBLE);
	    	findViewById(android.R.id.list).setVisibility(View.VISIBLE);
	    	//findViewById(R.id.main_pub_title).setVisibility(View.VISIBLE);
		}
    	
    }
    
    public void onClick(View v)
    {
    	Intent i;
    	Bundle b = new Bundle();
    	switch (v.getId()) {
    		case R.id.organise_button : 
    		{
    			
    			i = new Intent(this, Pending.class);
    			startActivityForResult(i,Constants.FromPending);
    			break;
    		}
    		case R.id.invites_button : {

    			i = new Intent(this, CurrentEvents.class);
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
    
    public void runHere(Runnable r)
    {
    	runOnUiThread(r);
    }
    
    private ServiceConnection connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			LaunchApplication.this.service = (IPubService)service;
		}

		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			
		}

    };
}
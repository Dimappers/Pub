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
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//need to log into Facebook if not logged in before
    	setContentView(R.layout.main);
    	Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG).show();
    	
    	Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setOnClickListener(this);
    	
    	Button button_invites = (Button)findViewById(R.id.invites_button);
    	button_invites.setOnClickListener(this);
    	
    }
    public void onClick(View v)
    {
    	Intent i;
		
		switch (v.getId()){
		case R.id.organise_button : 
		{
			i = new Intent(this, Organise.class);
			//TODO KB
			//Below: trying to work out how to add the currentlocation to the next screen
			//maybe need to create a new intent that does this.. somehow - google it!
			//this may need to be put into Organise.java - then could use same location to discover people..
			//android.intent.action.EDIT ??
			//i = new Intent(, test://organise_screen/current_location");
			startActivity(i);
			break;
		}
		case R.id.invites_button :
			break;
		}
    }
}
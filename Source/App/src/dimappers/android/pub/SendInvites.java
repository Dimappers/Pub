package dimappers.android.pub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SendInvites extends Activity implements OnClickListener{

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.send_invite);
		
		Button button_send_invites = (Button)findViewById(R.id.send_Invites);
    	button_send_invites.setOnClickListener(this);
    	
    	Button button_delete_event = (Button) findViewById(R.id.delete_Event);
    	button_delete_event.setOnClickListener(this);
	}
	
	public void onClick(View v)
	{
		Intent i;
		
		switch (v.getId()) {
		case R.id.send_Invites : 
		{
			i = new Intent(this, HostingEvents.class);
			startActivity(i);
			break;
		}
		case R.id.delete_Event :
		{
			displayAlert();
		}
		}
    }
	
	public  void displayAlert()
    {
     new AlertDialog.Builder(this).setMessage("Are you sure you want to delete this event?")  
           .setTitle("Alert")  
           .setCancelable(true)  
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                finish();
                //TODO: Actually deletes the event!!!
           }
           })
           .setNegativeButton("No", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
           })
           .show(); 
           
    }
}

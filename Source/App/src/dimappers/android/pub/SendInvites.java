package dimappers.android.pub;

import java.util.Date;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SendInvites extends Activity implements OnClickListener{

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.send_invite);
		
		Button button_send_invites = (Button)findViewById(R.id.send_Invites);
    	button_send_invites.setOnClickListener(this);
    	
    	Button button_make_comment = (Button) findViewById(R.id.make_a_comment);
    	button_make_comment.setOnClickListener(this);
    	
    	Button button_edit = (Button) findViewById(R.id.edit_button);
    	button_edit.setOnClickListener(this);
    	
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
			break;
		}
		case R.id.edit_button :
		{
			PubEvent event = new PubEvent(new Date(), new User(new Integer(1)));
			
			Bundle bundle = new Bundle();
			bundle.putSerializable("event", event);
			bundle.putInt("test", 1992);
			i = new Intent(this, Organise.class);
			i.putExtras(bundle);
			startActivity(i);
			break;
		}
		case R.id.make_a_comment :
		{
			showAddDialog();
			break;
		}
		}
    }
	
	private void showAddDialog() 
	{
		 final Dialog commentDialog = new Dialog(SendInvites.this);
         commentDialog.setContentView(R.layout.making_comment);
         commentDialog.setTitle("Do you want to make a comment?");
         commentDialog.setCancelable(true);
		
        TextView text = (TextView) commentDialog.findViewById(R.id.comment_text_box);

		Button attachButton = (Button) commentDialog.findViewById(R.id.attach); 
		Button cancelButton = (Button) commentDialog.findViewById(R.id.cancel); 

		attachButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) { 

		Toast.makeText(getBaseContext(), "Make a comment", 
		Toast.LENGTH_LONG).show(); 
		} 
		}); 

		cancelButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) { 
		commentDialog.dismiss(); 
		} 
		});
		
		commentDialog.show();
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

package dimappers.android.pub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Organise extends Activity implements OnClickListener{
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.organise);
	    	
	    	Button button_organise = (Button)findViewById(R.id.location_button);
	    	button_organise.setOnClickListener(this);
	    	Button button_choose_guests = (Button)findViewById(R.id.chosen_guests_button);
	    	button_choose_guests.setOnClickListener(this);
	    	Button button_choose_time = (Button)findViewById(R.id.time_button);
	    	button_choose_time.setOnClickListener(this);
	    	Button button_save_event = (Button)findViewById(R.id.save_event);
	    	button_save_event.setOnClickListener(this);
	    	Button button_send_invites = (Button)findViewById(R.id.send_invites_event);
	    	button_send_invites.setOnClickListener(this);
	 }
	 
	 public void onClick(View v)
	 {
		 Intent i;
		 switch (v.getId()){
			case R.id.location_button : {
				i = new Intent(this, ChoosePub.class);
				startActivity(i);
				break;
			}
			case R.id.chosen_guests_button : {
				i = new Intent(this, Pending.class);
				startActivityForResult(i, 0);
				break;
			}
			case R.id.time_button : {
				i = new Intent(this, ChooseTime.class);
				startActivity(i);
				break;
			}
			case R.id.save_event : {
				//TODO: save event details
				i = new Intent(this, Events.class);
				startActivity(i);
				break;
			}
			case R.id.send_invites_event : {
				//TODO: save event details, then send invites to server
				i = new Intent(this, Events.class);
				startActivity(i);
				break;
			}
		 }
	 }
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if(requestCode==0)
		 {
			 super.onActivityResult(requestCode, resultCode, data);
			 Intent i = new Intent(this, Guests.class);	
			 startActivity(i);
		 }
	 }
}

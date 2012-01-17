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
	    	Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG).show();
	    	
	    	Button button_organise = (Button)findViewById(R.id.chosen_guests_button);
	    	button_organise.setOnClickListener(this);
	 }
	 
	 public void onClick(View v)
	 {
		 Intent i;
		 switch (v.getId()){
			case R.id.chosen_guests_button : {
				i = new Intent(this, Guests.class);
				startActivityForResult(i, 0);
				break;
			}
		 }
	 }

}

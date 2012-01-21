package dimappers.android.pub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class Guests extends Activity implements OnClickListener{

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guests);
    	
    	//TODO: When not just "Test Guest", need to have different checkboxes & cases in the switch for each
    	//Could extend onClickListener for each guest in this class & extend classes within constructors for add_guest/save buttons
    	CheckBox button_organise = (CheckBox)findViewById(R.id.checkBox1);
    	button_organise.setOnClickListener(this);
    	
    	Button button_add_guest = (Button)findViewById(R.id.add_guest);
    	button_add_guest.setOnClickListener(this);
    	Button save = (Button)findViewById(R.id.save);
    	save.setOnClickListener(this);
 }
	public void onClick(View v){
		Intent i;
		switch(v.getId())
		{
		case R.id.save : {
			//TODO: save details
			Toast.makeText(getApplicationContext(), "save test", Toast.LENGTH_SHORT);
			finish();
			break;
		}	
		case R.id.checkBox1 : {
			Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_LONG).show();
			break;
		}
		case R.id.add_guest : {
			i = new Intent(this, ChooseGuest.class);
			startActivity(i);
			break;
		}
		}
	}
}

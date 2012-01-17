package dimappers.android.pub;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Guests extends Activity implements OnClickListener{

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guests);
    	
    	//TODO: When not just "Test Guest", need to have different checkboxes & cases in the switch for each
    	Button button_organise = (Button)findViewById(R.id.checkBox1);
    	button_organise.setOnClickListener(this);
 }
	public void onClick(View v){
		switch(v.getId())
		{
		case R.id.save : {
			//TODO: save details
			//TODO: go back to previous screen on click
			break;
		}	
		case R.id.checkBox1 : {
			Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_LONG).show();
			break;
		}
		}
	}
}

package dimappers.android.pub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ChooseGuest extends Activity implements OnClickListener {
	
	String chosenguest;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guest_choose);
    	
    	EditText guest_input = (EditText)findViewById(R.id.input_guest);
    	
    	Button use_guest = (Button)findViewById(R.id.use_guests_button);
    	use_guest.setOnClickListener(this);
    }
    public void onClick(View v) {
    	switch(v.getId())
    	{
    	case R.id.use_guests_button :
    	{
    		//save guest
    		finish();
    		break;
    	}
    	}
    }
}

//This line will save the text written in the guest_input EditText as a String
//chosenguest = guest_input.getText().toString();

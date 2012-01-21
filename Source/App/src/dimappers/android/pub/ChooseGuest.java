package dimappers.android.pub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChooseGuest extends Activity implements OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guest_choose);
    	
    	((Button)findViewById(R.id.use_guests_button)).setOnClickListener(this);
    }
    public void onClick(View v) {
    	switch(v.getId())
    	{
    	case R.id.use_guests_button :
    	{
    		//save guest
    		//FIXME: save button not working
    		finish();
    	}
    	}
    }
}

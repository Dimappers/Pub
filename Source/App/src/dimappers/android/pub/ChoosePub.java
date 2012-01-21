package dimappers.android.pub;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChoosePub extends Activity	implements OnClickListener {
		
		 @Override
		    public void onCreate(Bundle savedInstanceState) {
		    	super.onCreate(savedInstanceState);
		    	setContentView(R.layout.pub_choose);
		    	
		    	((Button)findViewById(R.id.use_pub_button)).setOnClickListener(this);
		 }
		 
		 public void onClick(View v) {
			 switch(v.getId()) {
			 case R.id.use_guests_button :{
				 //TODO: save pub choice
				 //FIXME: save button not working
				 finish();
			 }
			 }
		 }
}

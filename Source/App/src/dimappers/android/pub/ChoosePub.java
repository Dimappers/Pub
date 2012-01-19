package dimappers.android.pub;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ChoosePub extends Activity	implements OnClickListener {
		
		 @Override
		    public void onCreate(Bundle savedInstanceState) {
		    	super.onCreate(savedInstanceState);
		    	setContentView(R.layout.pub_choose);
		 }
		 
		 public void onClick(View v) {
			 //TODO: make buttons do something
		 }
}

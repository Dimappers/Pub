package dimappers.android.pub;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class Organise extends Activity {
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.organise);
	    	Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG).show();
	 }

}

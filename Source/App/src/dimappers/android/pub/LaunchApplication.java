package dimappers.android.pub;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class LaunchApplication extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.home);
    	//LinearLayout organise = (LinearLayout)findViewById(R.id.organise_screen);
    	//organise.removeView(organise);
    
    	/*Button button_organise = (Button)findViewById(R.id.organise_button);
    	button_organise.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			LinearLayout organise = (LinearLayout)findViewById(R.id.organise_screen);
    			organise.addView(getLayoutInflater().inflate(R.layout.organise, null));
    		}
    	});*/
    }
}
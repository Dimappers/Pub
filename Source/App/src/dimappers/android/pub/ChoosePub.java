package dimappers.android.pub;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChoosePub extends Activity	implements OnClickListener {
		
		EditText pub_input;
		String entered_pub;
	
		 @Override
		    public void onCreate(Bundle savedInstanceState) {
		    	super.onCreate(savedInstanceState);
		    	setContentView(R.layout.pub_choose);
		    	
		    	pub_input = (EditText)findViewById(R.id.input_pub);
		    	
		    	Button use_pub = (Button)findViewById(R.id.use_pub_button);
		    	use_pub.setOnClickListener(this);
		 }
		 
		 public void onClick(View v) {
			 switch(v.getId()) {
			 case R.id.use_pub_button : {
				 //TODO: save pub choice
				 finish();
				 break;
			 }
			 }
		 }
		 
		 //this line will get the value of the text entered into the text box
		 //entered_pub = pub_input.getText().toString();
}

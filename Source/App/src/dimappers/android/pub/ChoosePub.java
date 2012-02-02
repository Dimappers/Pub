package dimappers.android.pub;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChoosePub extends Activity implements OnClickListener {
		
		EditText pub_input;
		PubEvent event;
		
		/*Info for GoogleMaps:
		 *Currently using debug fingerprint:
		 *http://code.google.com/android/add-ons/google-apis/mapkey.html
		 *keytool -v -list -alias androiddebugkey -keystore debug.keystore -storepass android -keypass android
		 *MD5 CERTIFICATE: 3B:05:12:7D:DF:18:C3:A2:ED:EF:74:CF:FB:80:E7:F7
		 *API KEY: 06fMFhCUyUDwPs7xO1tbEuiMgxLZPfhL8dSYGxA
		*/
	
		 @Override
		    public void onCreate(Bundle savedInstanceState) {
		    	super.onCreate(savedInstanceState);
		    	setContentView(R.layout.pub_choose);
		    	
		    	event = (PubEvent) getIntent().getExtras().getSerializable("event");
		    	
		    	pub_input = (EditText)findViewById(R.id.input_pub);
		    	pub_input.addTextChangedListener(new TextWatcher(){
					public void afterTextChanged(Editable arg0) {
						Toast.makeText(getApplicationContext(),pub_input.getText().toString(),Toast.LENGTH_LONG).show();
						}
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {		
					}
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
		    		});
		    	pub_input.setOnClickListener(this);

		    	Button use_pub = (Button)findViewById(R.id.use_pub_button);
		    	use_pub.setOnClickListener(this);
		 }
		 
		 public void onClick(View v) {
			 switch(v.getId()) {
			 case R.id.use_pub_button : {
				 event.SetPubLocation(new PubLocation(0,0,pub_input.getText().toString()));
				 Bundle b = new Bundle();
				 b.putSerializable("eventt", event);
				 Intent i = new Intent();
				 i.putExtras(b);
				 this.setResult(RESULT_OK,i);
				 finish();
				 break;
			 }
			 case R.id.input_pub : {
					if(pub_input.getText().toString() == findViewById(R.string.default_location).toString()) {pub_input.setText("");}
			 }
			 }
		 }
		 
		 //this line will get the value of the text entered into the text box
		 //entered_pub = pub_input.getText().toString();
}


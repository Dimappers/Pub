package dimappers.android.pub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.ApacheHttpTransport;

import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChoosePub extends ListActivity implements OnClickListener {
		
		EditText pub_input;
		PubEvent event;
		ListView pub_list;
		ArrayAdapter<String> adapter;
		ArrayList<String> listItems = new ArrayList<String>();

		double latitude;
		double longitude;
				
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
		    	latitude = getIntent().getExtras().getDouble("lat");
		    	longitude = getIntent().getExtras().getDouble("long");
		    	
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
		    	
		    	getPubs();
		    	
		    	pub_list = (ListView)findViewById(android.R.id.list);
				adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
				setListAdapter(adapter);

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
			 }
		 }
		 
		 public void getPubs() {
			 try {
				PubFinder finder = new PubFinder(latitude,longitude);
				List<Place> places = finder.performSearch();
				for(Place p: places) {
					listItems.add(p.toString());
				}
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				Log.d("ERROR","InvalidKeyException");
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				Log.d("ERROR","NoSuchAlgorithmException");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Log.d("ERROR","URISyntaxException");
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d("ERROR","Exception");
				e.printStackTrace();
			}
		 }
}


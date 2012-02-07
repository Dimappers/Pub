package dimappers.android.pub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.ApacheHttpTransport;

import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;
import dimappers.android.PubData.PubLocation;
import dimappers.android.pub.PlacesAutocompleteList.PlaceAutoComplete;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChoosePub extends ListActivity implements OnClickListener {
		
		EditText pub_input;
		PubEvent event;
		ListView pub_list;
		ArrayAdapter<Place> adapter;
		ArrayList<Place> listItems = new ArrayList<Place>();
		
		PubFinder finder;

		double latitude;
		double longitude;
	
		 @Override
		    public void onCreate(Bundle savedInstanceState) {
		    	super.onCreate(savedInstanceState);
		    	setContentView(R.layout.pub_choose);
		    	
		    	event = (PubEvent) getIntent().getExtras().getSerializable("event");
		    	latitude = getIntent().getExtras().getDouble("lat");
		    	longitude = getIntent().getExtras().getDouble("long");
		    	
		    	pub_input = (EditText)findViewById(R.id.input_pub);
		    	
				finder = new PubFinder(latitude,longitude);
		    	getPubs();
		    	
		    	pub_list = (ListView)findViewById(android.R.id.list);
				adapter = new ArrayAdapter<Place>(this, android.R.layout.simple_list_item_1, listItems);
				setListAdapter(adapter);

		    	Button use_pub = (Button)findViewById(R.id.use_pub_button);
		    	use_pub.setOnClickListener(this);
		 }
		 
		 public void onClick(View v) {
			 switch(v.getId()) {
				 case R.id.use_pub_button : {
					 getPubs(pub_input.getText().toString());
					break;
				 }
			 }
		 }
		 public void onListItemClick(ListView l, View v, int pos, long id) {
			 super.onListItemClick(l,v,pos,id);
			 Place place = listItems.get(pos);
			 event.SetPubLocation(
					 new PubLocation(
							 place.geometry.location.lat,
							 place.geometry.location.lng,
							 place.name)
					 );
			 Place_Detailed pd = null;
			try {
				pd = finder.performDetails(listItems.get(pos).reference).result;
			} catch (Exception e) {
				Log.d(Constants.MsgInfo,"Exception thrown from performDetails(...)");
				e.printStackTrace();
			}
			 Log.d(Constants.MsgInfo, pd.name + " has a rating of " + pd.rating);
			 Intent i = new Intent();
			 i.putExtra(Constants.CurrentWorkingEvent, event);
			 this.setResult(RESULT_OK,i);
			 finish();
		 }
		 
		 private void getPubs() {getPubs("");}
		 
		 private void getPubs(String keyword) {
			 try {
				List<Place> places = finder.performSearch(keyword);
				listItems.clear();
				for(Place p: places) {
					listItems.add(p);
				}
			} catch (InvalidKeyException e) {
				Log.d(Constants.MsgInfo,"InvalidKeyException");
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				Log.d(Constants.MsgInfo,"NoSuchAlgorithmException");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				Log.d(Constants.MsgInfo,"URISyntaxException");
				e.printStackTrace();
			} catch (Exception e) {
				Log.d(Constants.MsgInfo,"Exception");
				e.printStackTrace();
			}
		 }
}


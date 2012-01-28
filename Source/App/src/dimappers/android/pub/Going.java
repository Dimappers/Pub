package dimappers.android.pub;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;


public class Going extends Activity implements OnClickListener{

	ArrayList<String> listItems=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	ListView guest_list;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.going);
		
		Guests going_guests = new Guests();
	
		Collections.copy(listItems,going_guests.listItems);
    	guest_list = (ListView)findViewById(R.id.list_of_guests);
		guest_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		//adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, listItems);
		//setListAdapter(adapter);
		
		Button button_make_comment = (Button) findViewById(R.id.make_a_comment);
    	button_make_comment.setOnClickListener(this);

	}
	
	public void onClick(View v)
	{
		Intent i;
		
		switch (v.getId()) {
		
		case R.id.make_a_comment :
		{
			showAddDialog();
			break;
		}
		}
		
	}
	
	public void onResume(View v){
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	private void showAddDialog() 
	{
		 final Dialog commentDialog = new Dialog(Going.this);
         commentDialog.setContentView(R.layout.making_comment);
         commentDialog.setTitle("Do you want to make a comment?");
         commentDialog.setCancelable(true);
		
        TextView text = (TextView) commentDialog.findViewById(R.id.comment_text_box);

		Button attachButton = (Button) commentDialog.findViewById(R.id.attach); 
		Button cancelButton = (Button) commentDialog.findViewById(R.id.cancel); 

		attachButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) { 

		Toast.makeText(getBaseContext(), "Make a comment", 
		Toast.LENGTH_LONG).show(); 
		} 
		}); 

		cancelButton.setOnClickListener(new OnClickListener() { 
		// @Override 
		public void onClick(View v) { 
		commentDialog.dismiss(); 
		} 
		});
		
		commentDialog.show();
	}
	
	public void onContentChange() {super.onContentChanged();}
	//public void onlistItemClick(ListView l, View v, int pos, long id) {super.onListItemClick(l, v, pos, id);}
	
}

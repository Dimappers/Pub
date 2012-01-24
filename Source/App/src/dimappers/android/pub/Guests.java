package dimappers.android.pub;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class Guests extends ListActivity implements OnClickListener{
	ArrayList<String> listItems=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	ListView guest_list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.guests);
    	
    	//TODO: When not just "Test Guest", need to have different checkboxes & cases in the switch for each
    	//Could extend onClickListener for each guest in this class & extend classes within constructors for add_guest/save buttons

    	listItems.add("kim");
    	listItems.add("jason");
    	listItems.add("tom");
    	listItems.add("mark");
    	listItems.add("tom");
    	
    	guest_list = (ListView)findViewById(android.R.id.list);
		guest_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, listItems);
		setListAdapter(adapter);
    	
    	Button button_add_guest = (Button)findViewById(R.id.add_guest);
    	button_add_guest.setOnClickListener(this);
    	Button save = (Button)findViewById(R.id.save);
    	save.setOnClickListener(this);
	}
	public void onResume(View v){
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	public void onClick(View v){
		Intent i;
		switch(v.getId())
		{
		case R.id.save : {
			//TODO: save details
			finish();
			break;
		}
		case R.id.add_guest : {
			i = new Intent(this, ChooseGuest.class);
			startActivity(i);
			break;
		}
		}
	}
	public void onContentChange() {super.onContentChanged();}
	public void onListItemClick(ListView l, View v, int pos, long id) {super.onListItemClick(l, v, pos, id);}
}

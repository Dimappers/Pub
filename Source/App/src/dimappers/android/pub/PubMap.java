package dimappers.android.pub;

import org.jdom.Element;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class PubMap extends /*Map*/Activity {

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Place place = new Place((Element)getIntent().getSerializableExtra("place"));
		
		setContentView(R.layout.pub_map);
	}
}

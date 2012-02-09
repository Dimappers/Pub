package dimappers.android.pub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NoInternet extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.no_internet);
    	
    	((Button)findViewById(R.id.nointernetokbutton)).setOnClickListener(this);
	}
			public void onClick(View v) {
				switch(v.getId())
				{
				case R.id.nointernetokbutton : {
					this.setResult(RESULT_OK,new Intent());
					finish();
				}
				}
				
			}
	
}

package dimappers.android.pub;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ToSend extends Activity implements OnClickListener{

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.to_send);
		
    	findViewById(R.id.textView7).setVisibility(View.GONE);
    	findViewById(R.id.editText1).setVisibility(View.GONE);
    	findViewById(R.id.make_a_comment).setVisibility(View.GONE);
    	findViewById(R.id.send).setVisibility(View.GONE);
    	
    	Button button_going = (Button)findViewById(R.id.going);
    	button_going.setOnClickListener(this);
    	
    	Button button_decline = (Button) findViewById(R.id.decline);
    	button_decline.setOnClickListener(this);
    	
    	Button button_make_comment = (Button) findViewById(R.id.make_a_comment);
    	button_make_comment.setOnClickListener(this);

	}
	
	public void onClick(View v)
	{
		Intent i;
		
		switch (v.getId()) {
		case R.id.going : 
		{
			findViewById(R.id.textView7).setVisibility(View.VISIBLE);
	    	findViewById(R.id.editText1).setVisibility(View.VISIBLE);
	    	findViewById(R.id.make_a_comment).setVisibility(View.VISIBLE);
	    	findViewById(R.id.send).setVisibility(View.VISIBLE);
			break;
		}
		case R.id.decline :
		{
	    	findViewById(R.id.make_a_comment).setVisibility(View.VISIBLE);
	    	findViewById(R.id.send).setVisibility(View.VISIBLE);
	    	findViewById(R.id.textView7).setVisibility(View.GONE);
	    	findViewById(R.id.editText1).setVisibility(View.GONE);
			break;
		}
		case R.id.make_a_comment :
		{
			showAddDialog();
			break;
		}
		}
    }
	
	private void showAddDialog() 
	{
		 final Dialog commentDialog = new Dialog(ToSend.this);
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
	
	
	
}

package dimappers.android.pub;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import dimappers.android.PubData.PubEvent;

public class ChooseTime extends Activity implements OnClickListener{
	private PubEvent event;
	private DatePicker date_picker;
	private TimePicker time_picker;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.choose_start);
    	
    	Button button_save_date_time = (Button)findViewById(R.id.save_date_and_time);
    	button_save_date_time.setOnClickListener(this);
    	
    	Bundle b = getIntent().getExtras();
    	event = (PubEvent)b.getSerializable("event");
    	Calendar startTime = event.GetStartTime();
        
    	// Date
        date_picker = (DatePicker)findViewById(R.id.datePicker);
        date_picker.setOnClickListener(this);
        
    	date_picker.init(startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH), onDateChangedListener);
        
        // Time
        time_picker = (TimePicker)findViewById(R.id.timePicker);
        time_picker.setOnClickListener(this);
        
        time_picker.setCurrentHour(startTime.get(Calendar.HOUR_OF_DAY));
        time_picker.setCurrentMinute(startTime.get(Calendar.MINUTE));
        time_picker.setOnTimeChangedListener(onTimeChangedListener);
        
        Toast.makeText(getApplicationContext(), event.GetStartTime().getTime().toString(), Toast.LENGTH_LONG).show();
	}
	
    private boolean isInPast() {
    	Calendar currentDate = event.GetStartTime();
    	return currentDate.compareTo(Calendar.getInstance()) <= -1;
    }
    private boolean isStrangeTime() {
    	return event.GetStartTime().get(Calendar.HOUR_OF_DAY)<16;
    }
    private boolean tooFarAhead()
    {
    	Calendar c = Calendar.getInstance();
    	return event.GetStartTime().getTimeInMillis() - c.getTimeInMillis() > 604800000; //number of milliseconds in a week
    }
    public void onClick(View v) {
    	switch(v.getId())
    	{
    		case R.id.save_date_and_time : {
    			if(isInPast()) {alertPast();}
    			else if(tooFarAhead()) {alertFuture();}
    			else if(isStrangeTime()) {ask();}
    			else {returnTime();}
    			break;
    		}
    	}
    }
    private void returnTime() {
    	Bundle b = new Bundle();
		b.putSerializable("event",event);
		Intent returnIntent = new Intent();
		returnIntent.putExtras(b);
		this.setResult(RESULT_OK,returnIntent);
		finish();
    }
    private void alertPast() {
    	new AlertDialog.Builder(this).setMessage("This time is in the past. Please choose a different one.")  
        .setTitle("Error")  
        .setCancelable(false)  
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {dialog.cancel();}}).show(); 
    }
    private void alertFuture() {
    	new AlertDialog.Builder(this).setMessage("Cannot plan a trip for more than a week in the future.")  
        .setTitle("Error")  
        .setCancelable(false)  
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {dialog.cancel();}}).show(); 
    }
    private void ask() {
    	new AlertDialog.Builder(this).setMessage("You have selected " + event.GetFormattedStartTime() + ". Are you sure you want to use this time?")  
        .setTitle("Strange Time")  
        .setCancelable(false)  
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {returnTime(); dialog.cancel();}})
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {dialog.cancel();} 
        }).show(); 
    }
    
    private DatePicker.OnDateChangedListener onDateChangedListener = new DatePicker.OnDateChangedListener() {
		public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
			Calendar currentDate = event.GetStartTime();
			currentDate.set(Calendar.YEAR, newYear);
			currentDate.set(Calendar.MONTH, newMonth);
			currentDate.set(Calendar.DAY_OF_MONTH, newDay);
		}
	};
	
	private TimePicker.OnTimeChangedListener onTimeChangedListener = new TimePicker.OnTimeChangedListener() {
    	public void onTimeChanged(TimePicker view, int newHour, int newMinute) {   		
    		Calendar currentDate = event.GetStartTime();
    		currentDate.set(Calendar.HOUR_OF_DAY, newHour);
    		currentDate.set(Calendar.MINUTE, newMinute);
    	}
    };
}

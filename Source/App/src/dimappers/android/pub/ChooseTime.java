package dimappers.android.pub;

import java.util.Calendar;
import java.util.Date;

import dimappers.android.PubData.PubEvent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ChooseTime extends Activity implements OnClickListener{
	private PubEvent event;
	private DatePicker date_picker;
	private DatePicker.OnDateChangedListener onDateChangedListener = new DatePicker.OnDateChangedListener() {
		public void onDateChanged(DatePicker view, int newYear, int newMonth, int newDay) {
			yeartemp = newYear;
			monthtemp = newMonth;
			daytemp = newDay;
		}
	};
	private int year;
    private int month;
    private int day;
	private int yeartemp;
    private int monthtemp;
    private int daytemp;

    private Calendar currentDate;
    private TimePicker time_picker;
    private TimePicker.OnTimeChangedListener onTimeChangedListener = new TimePicker.OnTimeChangedListener() {
    	public void onTimeChanged(TimePicker view, int newHour, int newMinute) {
    		if(isStrangeTime(newHour)) {/*TODO: notify!*/}
    		hour = newHour;
    		minute = newMinute;
    	}
    };
    private int hour;
    private int minute;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.choose_start);
    	
    	Button button_save_date_time = (Button)findViewById(R.id.save_date_and_time);
    	button_save_date_time.setOnClickListener(this);
    	
    	Bundle b = getIntent().getExtras();
    	event = (PubEvent)b.getSerializable("event");
    	Date startTime = event.GetStartTime();

        currentDate = Calendar.getInstance();
        
    	// Date
        date_picker = (DatePicker)findViewById(R.id.datePicker);
        date_picker.setOnClickListener(this);
        year = startTime.getYear() + 1900;
        month = startTime.getMonth();
        day = startTime.getDate();
    	date_picker.init(year, month, day, onDateChangedListener);
        
        // Time
        time_picker = (TimePicker)findViewById(R.id.timePicker);
        time_picker.setOnClickListener(this);
        hour = startTime.getHours();
        minute = startTime.getMinutes();
        time_picker.setCurrentHour(new Integer(hour));
        time_picker.setCurrentMinute(new Integer(minute));
        time_picker.setOnTimeChangedListener(onTimeChangedListener);
        
        Toast.makeText(getApplicationContext(), event.GetStartTime().toString(), Toast.LENGTH_LONG).show();
	}

	private void updateEvent() {}
	
    private boolean isInPast(int year, int month, int day) {
    	if(currentDate.after(new Date(year-1900,month,day))) {return true;}
    	return false;
    }
    private boolean isStrangeTime(int hour) {
    	if(hour<16) {return true;} 
    	return false;
    }
    public void onClick(View v) {
    	switch(v.getId())
    	{
    		case R.id.save_date_and_time : {
    			if(isInPast(yeartemp,monthtemp,daytemp)) {alert();}
    			else { 
    				event.SetStartTime(new Date(year-1900,month,day,hour,minute));
	    			Toast.makeText(getApplicationContext(), "stored time: "+event.GetStartTime().toString(), Toast.LENGTH_LONG).show();
	    			/*Intent i = getIntent();
	    			Bundle b = i.getExtras();
	    			b.putSerializable("event",event);
					this.setResult(RESULT_OK,i);*/
	    			finish();
    			}
    			break;
    		}
    	}
    }
    private void alert() {
    	new AlertDialog.Builder(this).setMessage("This time is in the past. Please choose a different one.")  
        .setTitle("Alert")  
        .setCancelable(false)  
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {dialog.cancel();}}).show(); 
    }
}

package dimappers.android.pub;

import java.util.Calendar;

import dimappers.android.PubData.PubEvent;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
	static final int DATE_DIALOG_ID = 0;
	private Button date_button;
    private int year;
    private int month;
    private int day;
    private DatePickerDialog.OnDateSetListener listenerDateFromDialog =
            new DatePickerDialog.OnDateSetListener() {
        		public void onDateSet(DatePicker view, int year1, int monthOfYear, int dayOfMonth) {
    				year = year1;
        			month = monthOfYear;
        			day = dayOfMonth;
        			updateDisplay(DATE_DIALOG_ID);
        		}
    		};
    static final int TIME_DIALOG_ID = 1;
    private Button time_button;
    private int hour;
    private int minute;
    private TimePickerDialog.OnTimeSetListener listenerTimeFromDialog =
    	    new TimePickerDialog.OnTimeSetListener() {
    	        public void onTimeSet(TimePicker view, int hourOfDay, int minute1) {
    	            hour = hourOfDay;
    	            minute = minute1;
    	            updateDisplay(TIME_DIALOG_ID);
    	        }
    	    };
    static final int BOTH_DIALOG_ID = 2;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.choose_start);
    	
    	Button button_save_date_time = (Button)findViewById(R.id.save_date_and_time);
    	button_save_date_time.setOnClickListener(this);
    	
    	Bundle b = getIntent().getExtras();
    	event = (PubEvent)b.getSerializable("event");
    	
        //TODO: Getting date should be done by passing event's current start date
        final Calendar c = Calendar.getInstance(); //current date
        
    	// Date
        date_button = (Button)findViewById(R.id.date_selector);
        date_button.setOnClickListener(this);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        
        // Time
        time_button = (Button)findViewById(R.id.time_selector);
        time_button.setOnClickListener(this);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        
        updateDisplay(BOTH_DIALOG_ID);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    	case DATE_DIALOG_ID: {return new DatePickerDialog(this, listenerDateFromDialog, year, month, day);}
	    	case TIME_DIALOG_ID: {return new TimePickerDialog(this, listenerTimeFromDialog, hour, minute, false);}
	    }
	    return null;
	}
	private void updateDisplay(int dateortime) {
		//TODO: Should probably do some error checking that date is in future (& maybe not too far in the future) - use isInPast(...) below
        switch(dateortime) {
        case DATE_DIALOG_ID : {
        	date_button.setText(new StringBuilder().append(day).append("/").append(month + 1).append("/").append(year).append(" "));
        	break;
        	}
        case TIME_DIALOG_ID : {
        	time_button.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute))); 
        	break;
        }
        case BOTH_DIALOG_ID : {
        	date_button.setText(new StringBuilder().append(day).append("-").append(month + 1).append("-").append(year).append(" "));
        	time_button.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute))); 
        }
        }
	}
    private static String pad(int c) {
        if (c >= 10) {return String.valueOf(c);}
        else {return "0" + String.valueOf(c);}
    }
    private boolean isInPast(int year, int month, int day) {
    	return true;
    }
    public void onClick(View v) {
    	switch(v.getId())
    	{
    		case R.id.save_date_and_time : {
    			//TODO: get date & time
    			//TODO: save
    			finish();
    			break;
    		}
    		case R.id.date_selector : {
    			showDialog(DATE_DIALOG_ID);
    			break;
    		}
    		case R.id.time_selector : {
    			showDialog(TIME_DIALOG_ID);
    			break;
    		}
    	}
    }
}

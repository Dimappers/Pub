package dimappers.android.pub;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import dimappers.android.PubData.Constants;
import dimappers.android.PubData.PubEvent;

public class ChooseTime extends Activity implements OnClickListener{
	private PubEvent event;
	private DatePicker date_picker;
	
	//TimePicker:
		private Button currenthour;
		private Button currentminute;
		private Button ampm;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.choose_start);
    	
    	Button button_save_date_time = (Button)findViewById(R.id.save_date_and_time);
    	button_save_date_time.setOnClickListener(this);
    	
    	bindService(new Intent(getApplicationContext(), PubService.class), connection, 0);
        
    	// Date
        date_picker = (DatePicker)findViewById(R.id.datePicker);
        date_picker.setOnClickListener(this);
        
        // Time
        findViewById(R.id.hour_add).setOnClickListener(this);
        currenthour = (Button) findViewById(R.id.currenthour);
        currenthour.setOnClickListener(this);
        findViewById(R.id.hour_take).setOnClickListener(this);
        
        findViewById(R.id.minute_add).setOnClickListener(this);
        currentminute = (Button) findViewById(R.id.currentminute);
        currentminute.setOnClickListener(this);
        findViewById(R.id.minute_take).setOnClickListener(this);
        
        ampm = (Button) findViewById(R.id.am_pm);
        ampm.setOnClickListener(this);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		unbindService(connection);
	}

	private String getMinute(int minute) {
		if(minute<=7) {return "00";}
		else return "15";
	}
	
	private String getAmPm(int ampm) {
		if(ampm==1) {return "PM";}
		else return "AM";
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
    		
    		case R.id.hour_add :
    		{
    			Calendar previousTime = event.GetStartTime();
    			Date date = previousTime.getTime();
    			
    			if(date.getHours()<12) {
    				date.setHours(date.getHours()+1);
    				currenthour.setText(""+date.getHours());
    			}
    			else if(date.getHours()==12){
    				date.setHours(13);
    				currenthour.setText("1");
    				ampm.setText("PM");
    			}
    			else if(date.getHours()<23)
    			{
    				date.setHours(date.getHours()+1);
    				currenthour.setText(""+(date.getHours()-12));
    			}
    			else if(date.getHours()==23)
    			{
    				date.setHours(0);
    				currenthour.setText("12");
    				ampm.setText("AM");
    			}
    			
    			previousTime.setTime(date);
    			event.SetStartTime(previousTime);
    			break;
    		}
    		case R.id.currenthour :
    		{
    			break;
    		}
    		case R.id.hour_take : {
    			Calendar previousTime = event.GetStartTime();
				Date date = previousTime.getTime();
				
				if(date.getHours()==0) {
					date.setHours(23);
					currenthour.setText("11");
					ampm.setText("PM");
				}
				else if(date.getHours()==1){
					date.setHours(0);
					currenthour.setText("12");
				}
				else if(date.getHours()<=12)
				{
					date.setHours(date.getHours()-1);
					currenthour.setText(""+date.getHours());
				}
				else if(date.getHours()==13)
				{
					date.setHours(12);
					currenthour.setText("12");
					ampm.setText("AM");
				}
				else if(date.getHours()<=23)
				{
					date.setHours(date.getHours()-1);
					currenthour.setText(""+(date.getHours()-12));
				}
				
				previousTime.setTime(date);
				event.SetStartTime(previousTime);
				break;
			}
    		case R.id.minute_add :
    		{
    			Calendar previousTime = event.GetStartTime();
    			Date date = previousTime.getTime();
    			if(date.getMinutes()==0) {date.setMinutes(15); currentminute.setText("15");}
    			else if(date.getMinutes()==15) {date.setMinutes(30); currentminute.setText("30");}
    			else if(date.getMinutes()==30) {date.setMinutes(45); currentminute.setText("45");}
    			else if(date.getMinutes()==45)
    			{
    				date.setMinutes(0); 
    				currentminute.setText("00");
    				date.setHours(date.getHours()+1); 
    				if(date.getHours()<12)
    				{
    					if(date.getHours()==0){currenthour.setText("12"); ampm.setText("AM");}
    					else {currenthour.setText(""+date.getHours());}
    				}
    				else
    				{
    					if(date.getHours()==12) {ampm.setText("PM");}
    					currenthour.setText(""+(date.getHours()-12));}
    			}
    			previousTime.setTime(date);
    			event.SetStartTime(previousTime);
    			break;
    		}
    		case R.id.currentminute :
    		{
    			break;
    		}
    		case R.id.minute_take :
    		{
    			Calendar previousTime = event.GetStartTime();
    			Date date = previousTime.getTime();
    			if(date.getMinutes()==0)
    			{
    				date.setMinutes(45); 
    				currentminute.setText("45");
    				if(date.getHours()==0) 
    				{
    					date.setHours(23);
    					ampm.setText("PM");
    					currenthour.setText("11");
    				}
    				else if(date.getHours()==12)
    				{
    					date.setHours(11);
    					ampm.setText("AM");
    					currenthour.setText("11");
    				}
    				else {
    					date.setHours(date.getHours()-1);
    					if(date.getHours()==0) {currenthour.setText("12");}
    					else{
    						if(date.getHours()<12) {currenthour.setText(""+date.getHours());}
    						else {currenthour.setText(""+(date.getHours()-12));}
    					}	
    				}
    			}
    			else if(date.getMinutes()==15) {date.setMinutes(0); currentminute.setText("00");}
    			else if(date.getMinutes()==30) {date.setMinutes(15); currentminute.setText("15");}
    			else if(date.getMinutes()==45) {date.setMinutes(30); currentminute.setText("30");}
    			previousTime.setTime(date);
    			event.SetStartTime(previousTime);
    			break;
    		}
    		case R.id.am_pm :
    		{
    			Calendar previousTime = event.GetStartTime();
    			Date date = previousTime.getTime();
    			if(date.getHours()<12) {date.setHours(date.getHours()+12); ampm.setText("PM");}
    			else {date.setHours(date.getHours()-12); ampm.setText("AM");}
    			previousTime.setTime(date);
    			event.SetStartTime(previousTime);
    			break;
    		}
    	}
    }
    private void returnTime() {
    	Bundle b = new Bundle();
		b.putInt(Constants.CurrentWorkingEvent,event.GetEventId());
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
    
    private ServiceConnection connection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName arg0, IBinder serviceBinder)
		{
			IPubService serviceInterface = (IPubService)serviceBinder;
			event = serviceInterface.getEvent(getIntent().getExtras().getInt(Constants.CurrentWorkingEvent));
			
			Calendar startTime = event.GetStartTime();
			
			date_picker.init(startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH), onDateChangedListener);
			
			currenthour.setText(""+startTime.get(Calendar.HOUR));
	        currentminute.setText(""+getMinute(startTime.get(Calendar.MINUTE)));
	        ampm.setText(""+getAmPm(startTime.get(Calendar.AM_PM)));
		}

		public void onServiceDisconnected(ComponentName arg0)
		{			
		}
		
	};
}

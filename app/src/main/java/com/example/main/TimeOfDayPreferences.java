package com.example.main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class TimeOfDayPreferences extends Activity implements OnItemSelectedListener{
	
	//the two textViews that will be used to display the start and stop times
	static TextView startTime;
	static TextView endTime;
	
	//Strings to use with the display
	static private String start="Start Time";
	static private String end="End Time";
	
	//This int will determine the start vs the end time
	static int whichOne;
	
	//must check to make sure the start time not after the end time. 
	//should set these both to 0 to start with, or something like that
	static int startMinutes=0;
	static int endMinutes=0;
	
	//get days of the week
	String daysOfWeek[]=new String[]{MySQLiteHelper.COLUMN_MONDAY,MySQLiteHelper.COLUMN_TUESDAY,MySQLiteHelper.COLUMN_WEDNESDAY,
			MySQLiteHelper.COLUMN_THURSDAY,MySQLiteHelper.COLUMN_FRIDAY,MySQLiteHelper.COLUMN_SATURDAY,MySQLiteHelper.COLUMN_SUNDAY
	};
	
	static String getTimes[]=new String[]{MySQLiteHelper.COLUMN_STARTTIME, MySQLiteHelper.COLUMN_ENDTIME};
	
	//this will get the initial prob and the preferred method of interruption
	static String getPreferences[]=new String[]{MySQLiteHelper.COLUMN_TODPROB, MySQLiteHelper.COLUMN_TODINTERRUPTIONPREF};
	
	
	//database information
	MySQLiteHelper helper;
	private static SQLiteDatabase database;
	
	//to get the correct information to display
	static long id;
	
	//whether or see if the time is appropriate to enter
	boolean evalStartTime;
	boolean evalEndTime;
	
	//the dates we compare and the format
	Date  startTimeDate;
	Date  endTimeDate;
	SimpleDateFormat format;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_of_day_preferences);
	
		//initially set the evaluations to false
		evalStartTime=false;
		evalEndTime=false;
		
		
		startTime=(TextView)findViewById(R.id.startTime);
		endTime=(TextView)findViewById(R.id.endTime);
		
		//set the initial texts of the two textViews 
		startTime.setText(start + "Not Set");
		endTime.setText(end + "Not Set");
		
		//get all the buttons I made (Can I make an arrayList?)
		ToggleButton monday=(ToggleButton)findViewById(R.id.toggleMonday);
		ToggleButton tuesday=(ToggleButton)findViewById(R.id.toggleTuesday);
		ToggleButton wednesday=(ToggleButton)findViewById(R.id.toggleWednesday);
		ToggleButton thursday=(ToggleButton)findViewById(R.id.toggleThursday);
		ToggleButton friday=(ToggleButton)findViewById(R.id.toggleFriday);
		ToggleButton saturday=(ToggleButton)findViewById(R.id.toggleSaturday);
		ToggleButton sunday=(ToggleButton)findViewById(R.id.toggleSunday);
		
		
		//create an arraylist of these button, I'm sure there is a better way
		List<ToggleButton> daysOfWeek=new ArrayList<ToggleButton>();
		daysOfWeek.add(monday);
		daysOfWeek.add(tuesday);
		daysOfWeek.add(wednesday);
		daysOfWeek.add(thursday);
		daysOfWeek.add(friday);
		daysOfWeek.add(saturday);
		daysOfWeek.add(sunday);
	
		//set up the connection to the database
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		
		//need to get the primary key from the intent. 
		id=getIntent().getLongExtra("sendKey", 0);
		
		//get all the states of the buttons 0=false 1=true
		int[] onOff=getDaysOfWeekPreference(id);
		
		//set the states of the buttons, they are in order, starting with Monday
		for(int i=0; i<daysOfWeek.size(); i++){
			if(onOff[i]==1){
				daysOfWeek.get(i).setChecked(true);
			}
			else{
				daysOfWeek.get(i).setChecked(false);
			}
		}
		
		//this array will store the saved values for the spinners
		int[] spinnerValue=getProb(id);
		int prob=spinnerValue[0];
		int callPref=spinnerValue[1];
		
		//this will create the spinners for the P(I) and the preferred method of interruption
		Spinner spinner=(Spinner)findViewById(R.id.tod_preference_spinner);
		
		Spinner callspinner=(Spinner)findViewById(R.id.tod_call_preference_spinner);
		//Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this, R.array.preference_spinner, 
				android.R.layout.simple_spinner_dropdown_item);
		
		//Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//Create an ArrayAdapter for the call preference spinner
		ArrayAdapter<CharSequence> callAdapter= ArrayAdapter.createFromResource(this, R.array.call_preference_spinner, 
				android.R.layout.simple_spinner_dropdown_item);
		
		callAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		callspinner.setAdapter(callAdapter);
		
		callspinner.setOnItemSelectedListener(this);

		callspinner.setSelection(callPref);
		//specifies the interface implemented
		spinner.setOnItemSelectedListener(this);
		
		//sets the initial value of the spinner
		spinner.setSelection(prob);
		
		
		format=new SimpleDateFormat("h:mm a");
		
		//set initial values for start and end date
		try {
			startTimeDate=format.parse("12:00 am");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			endTimeDate=format.parse("11:59 pm");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//get the times that have been set
		Cursor cursor=database.query(MySQLiteHelper.TABLE_TOD_PROFILE,getTimes,MySQLiteHelper.COLUMN_TODID + " = " + id,null,null,null,null);
		cursor.moveToFirst();
		//if(cursor.getInt(0)!=null){
			startMinutes=cursor.getInt(0);
			startTime.setText(timeDisplay(cursor.getInt(0),start));
		/*	try {
				startTimeDate= format.parse((String) startTime.getText());
			} catch (ParseException e) {
				e.printStackTrace();
			}*/
		//}
		//if(cursor.getString(1)!=null){
			endMinutes=cursor.getInt(1);
			endTime.setText(timeDisplay(cursor.getInt(1),end));
			/*try {
				endTimeDate= format.parse((String) endTime.getText());
			} catch (ParseException e) {
				e.printStackTrace();
			}*/
		//}
		cursor.close();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.time_of_day_preferences, menu);
		return true;
	}
	
	public void enterTime(View v){
		DialogFragment newFragment = new TimePickerFragment();
		switch(v.getId()){
		case R.id.setStartTime:
			whichOne=1;
			newFragment.show(getFragmentManager(), "timePicker");
			break;
		case R.id.setEndTime:
			whichOne=2;
			newFragment.show(getFragmentManager(), "timePicker");
			break;
		}
		
	}
	
	
	//nested fragment
	public static class TimePickerFragment extends DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	// Use the current time as the default values for the picker
	final Calendar c = Calendar.getInstance();
	int hour = c.get(Calendar.HOUR_OF_DAY);
	int minute = c.get(Calendar.MINUTE);

	// Create a new instance of TimePickerDialog and return it
	return new TimePickerDialog(getActivity(), this, hour, minute,
	DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
		int evalTime=hourOfDay*60+minute;
		String amPM="AM";
		if(hourOfDay==0){
			hourOfDay=12;
		}
		if(hourOfDay >=12){
			amPM="PM";
		}
		if(hourOfDay>12){
			hourOfDay-=12;
		}
		
		String filler="";
		if((minute%60)<10){
			filler="0";
		}
		
		if(whichOne==1){
			if(evalTime>endMinutes){
				Toast.makeText(getActivity(), "Please have the start time be before the end time", Toast.LENGTH_SHORT).show();
			}
			startTime.setText(start + " " + hourOfDay + " : " + filler + minute + " " + amPM);
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_STARTTIME, evalTime);
			database.update(MySQLiteHelper.TABLE_TOD_PROFILE, values, MySQLiteHelper.COLUMN_TODID + " = " + id,null);	
			}
			
		if(whichOne==2){
			if(evalTime<startMinutes){
				Toast.makeText(getActivity(), "Please have the end time after the start time", Toast.LENGTH_SHORT).show();
			}
			endTime.setText(end + " " + hourOfDay + " : " + filler + minute + " " + amPM);
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_ENDTIME, evalTime);
			database.update(MySQLiteHelper.TABLE_TOD_PROFILE, values, MySQLiteHelper.COLUMN_TODID + " = " + id,null);	
			}
			
		}
	}
	
	public void onToggleClicked(View v){
		int buttonPosition;
		if(((ToggleButton)v).isChecked()){
			buttonPosition=1;
		}
		else{
			buttonPosition=0;
		}
		switch(v.getId()){
		case R.id.toggleMonday:
			updatePreference(MySQLiteHelper.COLUMN_MONDAY,buttonPosition);
			break;
		case R.id.toggleTuesday:
			updatePreference(MySQLiteHelper.COLUMN_TUESDAY,buttonPosition);
			break;
		case R.id.toggleWednesday:
			updatePreference(MySQLiteHelper.COLUMN_WEDNESDAY,buttonPosition);
			break;
		case R.id.toggleThursday:
			updatePreference(MySQLiteHelper.COLUMN_THURSDAY,buttonPosition);
			break;
		case R.id.toggleFriday:
			updatePreference(MySQLiteHelper.COLUMN_FRIDAY,buttonPosition);
			break;
		case R.id.toggleSaturday:
			updatePreference(MySQLiteHelper.COLUMN_SATURDAY,buttonPosition);
			break;
		case R.id.toggleSunday:
			updatePreference(MySQLiteHelper.COLUMN_SUNDAY,buttonPosition);
			break;
		}
	}
	
	public void updatePreference(String dow, int buttonPosition){
		ContentValues values = new ContentValues();
		values.put(dow, buttonPosition);
		database.update(MySQLiteHelper.TABLE_TOD_PROFILE, values, MySQLiteHelper.COLUMN_TODID + " = " + id,null);	
	}
	
	
	public int[] getDaysOfWeekPreference(long id){
		System.out.println("This is the id " + id);
		Cursor cursor=database.query(MySQLiteHelper.TABLE_TOD_PROFILE, daysOfWeek,MySQLiteHelper.COLUMN_TODID + " = " + id,null,null,null,null);
		cursor.moveToFirst();
		int[] values=new int[daysOfWeek.length];
		for(int i=0; i<daysOfWeek.length;i++){
			values[i]=cursor.getInt(i);
			System.out.println(values[i]);
		}
		cursor.close();
		return values;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long anid) {
		switch(parent.getId()){
		case R.id.tod_preference_spinner:
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_TODPROB, pos);
			database.update(MySQLiteHelper.TABLE_TOD_PROFILE, values, MySQLiteHelper.COLUMN_TODID + " = " + id, null);
			break;
		case R.id.tod_call_preference_spinner:
			ContentValues callvalues = new ContentValues();
			callvalues.put(MySQLiteHelper.COLUMN_TODINTERRUPTIONPREF, pos);
			database.update(MySQLiteHelper.TABLE_TOD_PROFILE, callvalues, MySQLiteHelper.COLUMN_TODID + " = " + id, null);
			break;
		}
		
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// if nothing is selected don't do anything
		
	}
	
	public int[] getProb(long id){
		int[] returnArray=new int[2];
		Cursor cursor=database.query(MySQLiteHelper.TABLE_TOD_PROFILE, getPreferences,MySQLiteHelper.COLUMN_TODID + " = " + id,null,null,null,null);
		cursor.moveToFirst();
		returnArray[0]=cursor.getInt(0);
		returnArray[1]=cursor.getInt(1);
		cursor.close();
		return returnArray;
	}
	
	public String timeDisplay(int i, String title){
		String display="";
		String amPM="AM";
		int hour;
		int minute;
		if(i>=12*60){
			amPM="PM";
		}
		hour=i/60;
		if((i/60)==0){
			hour=12;
		}
		if((i/60)>12){
			hour-=12;
		}
		minute=i%60;
		if(minute%60<10){
			display=title + ": " + Integer.toString(hour)+ " : " + "0"+Integer.toString(minute) + " " + amPM;
		}
		else{
		display=title + ": " + Integer.toString(hour)+ " : " + Integer.toString(minute) + " " + amPM;
		}
		return display;
		
	}
	
	@Override
	public void onStop(){
		super.onStop();
		database.close();
	}
	
	@Override
	public void onStart(){
		super.onStart();
		database=helper.getWritableDatabase();
	}

}

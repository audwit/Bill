package com.example.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ScheduleActivity extends Activity implements OnItemSelectedListener{
	//the key to get the saved preference for default probability
	public static final String DEFAULT_PROB="DefaultProb";
	public static final String DEFAULT_CALL_PREF="defaultCallPref";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		//need to get the default probability for schedule, as well as the default interruption method profile
		SharedPreferences settings=getSharedPreferences(DEFAULT_PROB, 0);
		SharedPreferences callPrefSettings=getSharedPreferences(DEFAULT_CALL_PREF,0);
		int defaultProb=settings.getInt("scheduleDefaultProb", 5);
		int defaultEventProb=settings.getInt("scheduleEventDefaultProb", 5);
		int defaultCallPref=callPrefSettings.getInt("scheduleDefaultCallPref", 0); 
		
		//populate the default event probability spinner with 1 through 10
		Spinner defaultEventProbSpinner=(Spinner)findViewById(R.id.scheduleEventDefaultProb);
		
		ArrayAdapter<CharSequence> spinnerAdapter= ArrayAdapter.createFromResource(this, R.array.preference_spinner, 
				android.R.layout.simple_spinner_dropdown_item);
		
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		defaultEventProbSpinner.setAdapter(spinnerAdapter);
		defaultEventProbSpinner.setSelection(defaultEventProb);
		defaultEventProbSpinner.setOnItemSelectedListener(this);
		
		//populate the method of interruption spinner with the appropriate options
		Spinner defaultMOISpinner=(Spinner)findViewById(R.id.spinnerScheduleEventDefaultMOI);
		

		ArrayAdapter<CharSequence> callPref= ArrayAdapter.createFromResource(this, R.array.call_preference_spinner, 
				android.R.layout.simple_spinner_dropdown_item);
		
		defaultMOISpinner.setAdapter(callPref);
		defaultMOISpinner.setSelection(defaultCallPref);
		defaultMOISpinner.setOnItemSelectedListener(this);
		
		//populate the default probability spinner with 1 through 10
		Spinner defaultProbSpinner=(Spinner)findViewById(R.id.scheduleDefaultProb);
				
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		defaultProbSpinner.setAdapter(spinnerAdapter);
		defaultProbSpinner.setSelection(defaultProb);
		defaultProbSpinner.setOnItemSelectedListener(this);
		
		
		
				
	}

	public void onItemSelected(AdapterView<?> parent, View view,
			int pos, long id){
		SharedPreferences preferences=getSharedPreferences(DEFAULT_PROB,0);
		SharedPreferences callPreferences=getSharedPreferences(DEFAULT_CALL_PREF,0);
		SharedPreferences.Editor editor=preferences.edit();
		SharedPreferences.Editor callEditor=callPreferences.edit();
		switch(parent.getId()){
		case R.id.scheduleDefaultProb:	
			editor.putInt("scheduleDefaultProb",pos);
			editor.commit();
		break;
		case R.id.scheduleEventDefaultProb:
			editor.putInt("scheduleEventDefaultProb",pos);
			editor.commit();
			break;
		case R.id.spinnerScheduleEventDefaultMOI:
			callEditor.putInt("scheduleDefaultCallPref", pos);
			callEditor.commit();
		}
		}
	
	//handles when nothing has been selected
		public void onNothingSelected(AdapterView<?> parent){
			//another callback interface
		}

}

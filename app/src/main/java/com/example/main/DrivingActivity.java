package com.example.main;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;


public class DrivingActivity extends Activity implements OnItemSelectedListener{
	public static final String DRIVING_SETTINGS="DriverSettings";
	public static final String DEFAULT_PROB="DefaultProb";
	public static final String TOGGLE="Toggle";
	
	ActivityScan scan;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driving);
		
		//Restore shaved preferences
		SharedPreferences drivingSettings=getSharedPreferences(DRIVING_SETTINGS,0);
		int mainDrivingProb=drivingSettings.getInt("mainDrivingProb", 5);
		int callPref=drivingSettings.getInt("callPreference", 0);
		
		//Restore the default preferences
		SharedPreferences defaultdrivingSettings=getSharedPreferences(DEFAULT_PROB, 0);
		int defaultDrivingProb=defaultdrivingSettings.getInt("drivingDefaultProb", 5);
		
		//create the spinner for the main driving P(I) (the non-default one)
		Spinner mainDrivingSpinner=(Spinner)findViewById(R.id.drivingSpinner);
		
		//create the spinner for the default driving P(I)
		Spinner defaultDrivingSpinner=(Spinner)findViewById(R.id.defaultDrivingSpinner);
		
		//create the spinner for the interruption preference
		Spinner interruptionPrefSpinner=(Spinner)findViewById(R.id.driving_call_preference_spinner);
		
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.preference_spinner,android.R.layout.simple_spinner_dropdown_item);
		
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		//sets up everything for the main driving spinner
		mainDrivingSpinner.setAdapter(spinnerAdapter);
		mainDrivingSpinner.setSelection(mainDrivingProb);
		mainDrivingSpinner.setOnItemSelectedListener(this);
		
		//sets up everything for the default driving spinner
		defaultDrivingSpinner.setAdapter(spinnerAdapter);
		defaultDrivingSpinner.setSelection(defaultDrivingProb);
		defaultDrivingSpinner.setOnItemSelectedListener(this);
		
		//Create an ArrayAdapter for the call preference spinner
		ArrayAdapter<CharSequence> callPreference= ArrayAdapter.createFromResource(this, R.array.call_preference_spinner, 
					android.R.layout.simple_spinner_dropdown_item);
				
		callPreference.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		interruptionPrefSpinner.setAdapter(callPreference);		
		interruptionPrefSpinner.setOnItemSelectedListener(this);
		interruptionPrefSpinner.setSelection(callPref);
		
		//the toggle button whether or not to have the scan always going
		ToggleButton toggleContext =(ToggleButton)findViewById(R.id.toggleDriving);
		
		//Get the saved position of the toggle button
		SharedPreferences toggleSettings=getSharedPreferences(TOGGLE,0);
		toggleContext.setChecked(toggleSettings.getBoolean("getToggle", false));
		
		//Instantiate the activityscan object
		scan=new ActivityScan(this);
				
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.driving, menu);
		return true;
	}
	
	public void onItemSelected(AdapterView<?> parent, View view,
			int pos, long id){
		//standard driving settings
		SharedPreferences preferences=getSharedPreferences(DRIVING_SETTINGS,0);
		SharedPreferences.Editor editor=preferences.edit();
		
		//default settings
		SharedPreferences defaultPreferences=getSharedPreferences(DEFAULT_PROB,0);
		SharedPreferences.Editor defaultEditor=defaultPreferences.edit();
		
		switch(parent.getId()){
		case R.id.drivingSpinner:	
			editor.putInt("mainDrivingProb",pos);
			editor.commit();
			break;
		case R.id.defaultDrivingSpinner:
			defaultEditor.putInt("drivingDefaultProb",pos);
			defaultEditor.commit();
			break;
		case R.id.driving_call_preference_spinner:
			editor.putInt("callPreference",pos);
			editor.commit();
			break;
		}
	
	}
	
	public void onNothingSelected(AdapterView<?> parent){
		//another callback interface
	}
	
	//takes care of the toggle button
	public void onToggleClicked(View v){
		//before starting the scan, needs to make sure location services is enabled.
		LocationManager lm=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		
		boolean locationServiceEnabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		SharedPreferences toggleSettings=getSharedPreferences(TOGGLE,0);
		SharedPreferences.Editor editor=toggleSettings.edit();
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)!=ConnectionResult.SUCCESS){
			Toast.makeText(this, "Please Download Google Play Services", Toast.LENGTH_LONG).show();
			((ToggleButton)v).setChecked(false);
			
		}else{
		//Check if toggle is on
		boolean on=((ToggleButton)v).isChecked();
		if(on){
			if(locationServiceEnabled){
				editor.putBoolean("getToggle", true);
				scan.startActivityRecognitionScan();
			}else{
				//Alert Dialog borrowed from http://stackoverflow.com/questions/10311834/android-dev-how-to-check-if-location-services-are-enabled
				 AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		            dialog.setMessage(this.getResources().getString(R.string.location_services_not_enabled));
		            dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {

		                @Override
		                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		                    // TODO Auto-generated method stub
		                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		                    startActivity(myIntent);
		                    //get gps
		                }
		            });
		            dialog.setNegativeButton(this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

		                @Override
		                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		                    // TODO Auto-generated method stub

		                }
		            });
		            dialog.show();
				((ToggleButton)v).setChecked(false);
			}
			
		}
		else{
			editor.putBoolean("getToggle", false);
			scan.stopActivityRecognitionScan();
		}
		editor.commit();
		}
	}


}

package com.example.main;

import java.util.Calendar;
import java.util.LinkedList;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognition extends IntentService{
	//this linked list will store the last 10 results of the recognition scan
	LinkedList<String> results=new LinkedList<String>();

	public static final String DRIVING_SETTINGS="DriverSettings";

	public ActivityRecognition() {
	super("ActivityRecognition");

	}

	/**
	* Google Play Services calls this once it has analyzed the sensor data
	*/
	@Override
	protected void onHandleIntent(Intent intent) {
	SharedPreferences preferences=getSharedPreferences(DRIVING_SETTINGS,0);
	SharedPreferences.Editor editor=preferences.edit();
	if (ActivityRecognitionResult.hasResult(intent)) {
	ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
	if(results.size()<=5){
	results.add(getFriendlyName(result.getMostProbableActivity().getType()));
	}
	else{
		results.remove();
		results.add(getFriendlyName(result.getMostProbableActivity().getType()));
	}
	for(int i=0; i<5;i++){
		//so while it does it's continuous, if it finds in vehicle in the last 10 entries, it assumes you are in a car and will set driving to true.
		//otherwise it will put it to false. This way there is no querying 
		if(results.contains("in vehicle")){
			editor.putBoolean("evaluateDriving",true);
			editor.commit();
		}
		else{
			editor.putBoolean("evaluateDriving",false);
			editor.commit();
		}
	}
	}
	}

	/**
	* When supplied with the integer representation of the activity returns the activity as friendly string
	* @param type the DetectedActivity.getType()
	* @return a friendly string of the
	*/
	private static String getFriendlyName(int detected_activity_type){
	switch (detected_activity_type ) {
	case DetectedActivity.IN_VEHICLE:
	return "in vehicle";
	case DetectedActivity.ON_BICYCLE:
	return "on bike";
	case DetectedActivity.ON_FOOT:
	return "on foot";
	case DetectedActivity.TILTING:
	return "tilting";
	case DetectedActivity.STILL:
	return "still";
	default:
	return "unknown";
	}
	}
}

package com.example.main;

import java.util.Calendar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

public class ActivityScan implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{
	private Context context;
	private static ActivityRecognitionClient mActivityRecognitionClient;
	private static PendingIntent callbackIntent;
	private static final long timeToUpdate=30000;
	public static final String DRIVING_SETTINGS="DriverSettings";

	public ActivityScan(Context context) {
	this.context=context;
	mActivityRecognitionClient	= new ActivityRecognitionClient(context, this, this);
	}

	/**
	* Call this to start a scan - don't forget to stop the scan once it's done.
	* Note the scan will not start immediately, because it needs to establish a connection with Google's servers - you'll be notified of this at onConnected
	*/
	public void startActivityRecognitionScan(){
	if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)==ConnectionResult.SUCCESS){
	mActivityRecognitionClient.connect();
	}
	else{
		System.out.println("Please Download Google Play Services");
	}
	}

	public void stopActivityRecognitionScan(){
	try{
	SharedPreferences preferences=context.getSharedPreferences(DRIVING_SETTINGS,0);
	SharedPreferences.Editor editor=preferences.edit();
	editor.putBoolean("evaluateDriving",false);
	editor.commit();
	System.out.println("I stopped");
	mActivityRecognitionClient.removeActivityUpdates(callbackIntent);
	mActivityRecognitionClient.disconnect();
	} catch (IllegalStateException e){
	// probably the scan was not set up, we'll ignore
	}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
	}

	/**
	* Connection established - start listening now
	*/
	@Override
	public void onConnected(Bundle connectionHint) {
	Intent intent = new Intent(context, ActivityRecognition.class);
	callbackIntent = PendingIntent.getService(context, 0, intent,
	PendingIntent.FLAG_UPDATE_CURRENT);
	mActivityRecognitionClient.requestActivityUpdates(timeToUpdate, callbackIntent);
	}

	@Override
	public void onDisconnected() {
		mActivityRecognitionClient=null;
	}


}

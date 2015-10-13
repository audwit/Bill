package com.example.main;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class ConnectLocationServiceIntent extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{

	LocationClient mLocationClient;
	
	@Override
	public void onCreate(){
		super.onCreate();
		System.out.println("I got here");
		mLocationClient=new LocationClient(this,this,this);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
	if(mLocationClient.isConnected()){
		System.out.println("Already connected");
	}else{
		mLocationClient.connect();
	}
	
	
	return Service.START_STICKY;
	}
	
	
	
	
	@Override
	public void onLocationChanged(Location arg0) {
		System.out.println("Location Updated");
		
	}
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		System.out.println("the connection failed");
		
	}
	@Override
	public void onConnected(Bundle arg0) {
		/*LocationRequest mLocationRequest=new LocationRequest();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(30000);
		mLocationRequest.setFastestInterval(0);
		mLocationClient.requestLocationUpdates(mLocationRequest, this);*/
		
	}
	@Override
	public void onDisconnected() {
		System.out.println("I disconnected");
		
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

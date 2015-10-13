//some code borrowed directly from http://stackoverflow.com/questions/7347871/how-to-reject-a-call-programatically-in-android?rq=1

package com.example.main;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {
	String mode="default";
	String phoneNumber;
	final static public String SWITCH_SETTINGS="switchSettings";
	@Override
	public void onReceive(Context context, Intent intent){
		//first thing to do is see if the app is desired on. If not, then nothing happens
		 SharedPreferences switchSettings=context.getSharedPreferences(SWITCH_SETTINGS,0);
		 boolean switchState=switchSettings.getBoolean("switchState", true);
		 
		 if(switchState){
		//for timing purposes, we want to see how long it takes from getting the intent, to actually setting the profile
		
			 
			 
		//set the state of the switch
		//why contexts instead of the class?
		String phoneState=intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		if(phoneState!=null){
		if(phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)){
			System.out.println("Get to broadcast receiver: " + System.nanoTime());
			phoneNumber=intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
			long startTime=System.nanoTime();
			Intent i=new Intent(context, Calculation.class);
			i.putExtra("phoneNumber", phoneNumber);
			i.putExtra("startTime",startTime);
			context.startService(i);
		}
		}
		//I don't know if this is the best code. This is looking respond to text message, but I don't know if it will work the same
		else{
			//got to get the bundle of info with the text message
			Bundle bundle=intent.getExtras();
			Object[] pdus=(Object[])bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for(int i=0; i<pdus.length; i++){
				messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
			}
			//Don't know if this is correct, but hopefully this will be triggered each time a text message is sent so it won't matter
			SmsMessage message=messages[0];
			phoneNumber=message.getOriginatingAddress();
			long startTime=System.nanoTime();
			Intent i=new Intent(context, Calculation.class);
			i.putExtra("phoneNumber", phoneNumber);
			i.putExtra("startTime", startTime);
			context.startService(i);
		}
		
		
		
		
		
	}else{
		System.out.println("The app is in the off state and will not run");
	}
		 
	}
	
}

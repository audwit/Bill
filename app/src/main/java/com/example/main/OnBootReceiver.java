package com.example.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startConnectService=new Intent(context, ConnectLocationServiceIntent.class);
		context.startService(startConnectService);
		System.out.println("I got to the boot receiver");
		
	}

}

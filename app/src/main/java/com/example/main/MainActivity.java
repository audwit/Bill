package com.example.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

	final static public String SWITCH_SETTINGS="switchSettings";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //upon launching the application, this is the first activity that comes up. It will broadcast an intent
        //that will picked up by the connectLocationServiceReceiver and that will in-turn connect to the location
        //service if not already connected.
        
        //Actually, I don't think the updates are having any impact. I think I will check when I get back to the apartment and see
        //if it does a better job approximating my location. 
        Intent connect=new Intent(this,ConnectLocationServiceIntent.class);
        this.startService(connect);
        
        //create the toggle button that will allow the user to turn off the application if they wish to do so 
        Switch switchAppOnOff=(Switch) findViewById(R.id.appOnOff);
        //get the shared preferences that will save the state of the button
        SharedPreferences switchSettings=getSharedPreferences(SWITCH_SETTINGS,0);
		boolean switchState=switchSettings.getBoolean("switchState", true);
		//set the state of the switch
		switchAppOnOff.setChecked(switchState);
		//need to set the state upon user selecting an option
		switchAppOnOff.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton switchView, boolean isChecked){
				SharedPreferences preferences=getSharedPreferences(SWITCH_SETTINGS,0);
				SharedPreferences.Editor editor=preferences.edit();
				if(isChecked){
					switchView.setChecked(true);
					editor.putBoolean("switchState", true);
					editor.commit();
				}
				else{
					switchView.setChecked(false);
					editor.putBoolean("switchState", false);
					editor.commit();
				}
				
			}
		});
        
        //creates the listview where the menu items will be added
        final ListView listview=(ListView) findViewById(R.id.ListView1);
        
        //the options for the menu
		final String options[]=new String[]{"Manage Location", "Manage Schedules", "Manage Time of Day", "Manage Driving Preferences",
				"Manage Contact Preferences", "Manage Weight Profiles", "Set Default Phone Profile", "Help" , "About"};
		
		//add the options to the adapter, in the simple list item 1 format
		final ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
		
		//applying the adapter to the listview
		listview.setAdapter(adapter);
		
		//when a menu item is clicked, it calls this method
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id){
				
				//The initial menu 
				if("Manage Location".equals(options[position])){
					Intent intent=new Intent(MainActivity.this, LocationActivity.class);
					startActivity(intent);
				}
				if("Manage Schedules".equals(options[position])){
					Intent intent=new Intent(MainActivity.this, ScheduleActivity.class);
					startActivity(intent);
				}
				if("Manage Time of Day".equals(options[position])){
					Intent intent=new Intent(MainActivity.this, TimeOfDayActivity.class);
					startActivity(intent);
				}
				if("Manage Driving Preferences".equals(options[position])){
					Intent intent=new Intent(MainActivity.this, DrivingActivity.class);
			    	startActivity(intent);
				}
				if("Manage Contact Preferences".equals(options[position])){
					Intent intent=new Intent(MainActivity.this, ContactActivity.class);
			    	startActivity(intent);
				}
				if("Manage Weight Profiles".equals(options[position])){
					Intent intent=new Intent(MainActivity.this, WeightsActivity.class);
			    	startActivity(intent);
				}
				if("Set Default Phone Profile".equals(options[position])){
					Intent intent=new Intent(MainActivity.this, DefaultProfileActivity.class);
			    	startActivity(intent);
				}
				if("Help".equals(options[position])){
					Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
				}
				if("About".equals(options[position])){
					Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
				}
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}

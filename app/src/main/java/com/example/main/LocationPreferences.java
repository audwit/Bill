package com.example.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class LocationPreferences extends Activity implements OnItemSelectedListener{
		//database information
		MySQLiteHelper helper;
		private static SQLiteDatabase database;
	
		//to get the correct information to display
		static long id;
		
		//this will get the initial prob and the preferred method of interruption
		static String locationPreferences[]=new String[]{MySQLiteHelper.COLUMN_LOCATIONPROB,MySQLiteHelper.COLUMN_LOCATIONINTERRUPTIONPREF};
		static String getSetLocation[]=new String[]{MySQLiteHelper.COLUMN_LOCATIONNAME};
		
		//all the objects needed for geocoder
		EditText address;
		Geocoder geocoder;
		List<Address> result;
		List<String> addressDisplay;
		ArrayAdapter<String> locationadapter;
		TextView currentlySetLocation;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_preferences);
		
		//instantiate the database connector
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		
		id=getIntent().getLongExtra("sendKey", 0);
		
		//will need to get the currently set location, or say please set location below if there is not location set. 
		currentlySetLocation=(TextView)findViewById(R.id.currentlySetLocation);
		currentlySetLocation.setText(getSetLocation());
		
		//this array will store the saved values for the spinners
		int[] spinnerValue=getProb(id);
		int prob=spinnerValue[0];
		int callPref=spinnerValue[1];
				
		//this will create the spinners for the P(I) and the preferred method of interruption
		Spinner spinner=(Spinner)findViewById(R.id.location_preference_spinner);
				
		Spinner callspinner=(Spinner)findViewById(R.id.location_call_preference_spinner);
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
		
		//this code deals with the geocoder and the results from the search
		address=(EditText)findViewById(R.id.address);
		
		//Create the geocoder that will allow for the look-up of a street address.
		geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());
		
		addressDisplay=new ArrayList<String>();
		
		ListView display=(ListView)findViewById(R.id.possibleLocations);
		
		locationadapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, addressDisplay);
		
		display.setAdapter(locationadapter);
		
		address.setOnKeyListener(new View.OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN)
					if((keyCode==KeyEvent.KEYCODE_DPAD_CENTER) ||
					(keyCode==KeyEvent.KEYCODE_ENTER)){
			
			//first thing to do is to clear the addressDisplay
			addressDisplay.clear();
			try{
			result=geocoder.getFromLocationName(address.getText().toString(), 5);
			}
			catch(IOException e){
			}
			address.setText("");
			
			String choices="";
			
			if(result!=null){
			//the location must have lat or long, otherwise they cannot be used.
			for(int i=0; i<result.size();i++){
				if(result.get(i).hasLatitude()==false){
					result.remove(i);
				}
			}
			
			for(int i=0;i<result.size(); i++){
				choices=result.get(i).getAddressLine(0)+"\n" + result.get(i).getAddressLine(1);
				addressDisplay.add(choices);
			}
			locationadapter.notifyDataSetChanged();
			}
			
			return true;
		}
				return false;
			}
		});
		
		display.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long anid){
			String locationDisplay=result.get(position).getAddressLine(0)+"\n" + result.get(position).getAddressLine(1);
			Double lat=result.get(position).getLatitude();
			Double longitude=result.get(position).getLongitude();
			currentlySetLocation.setText(locationDisplay);
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_LOCATIONNAME, locationDisplay);
			values.put(MySQLiteHelper.COLUMN_LOCATIONLAT, lat);
			values.put(MySQLiteHelper.COLUMN_LOCATIONLONG, longitude);
			database.update(MySQLiteHelper.TABLE_LOCATION, values, MySQLiteHelper.COLUMN_LOCATIONID + " = " + id, null);
			}
		});
		
				
	}

	private CharSequence getSetLocation() {
			Cursor cursor=database.query(MySQLiteHelper.TABLE_LOCATION,getSetLocation,MySQLiteHelper.COLUMN_LOCATIONID + " = "  + id,null,null,null,null);
			cursor.moveToFirst();
			String locale=cursor.getString(0);
			return (CharSequence)locale;
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_preferences, menu);
		return true;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long anid) {
		switch(parent.getId()){
		case R.id.location_preference_spinner:
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_LOCATIONPROB, pos);
			database.update(MySQLiteHelper.TABLE_LOCATION, values, MySQLiteHelper.COLUMN_LOCATIONID + " = " + id, null);
			break;
		case R.id.location_call_preference_spinner:
			ContentValues callvalues = new ContentValues();
			callvalues.put(MySQLiteHelper.COLUMN_LOCATIONINTERRUPTIONPREF, pos);
			database.update(MySQLiteHelper.TABLE_LOCATION, callvalues, MySQLiteHelper.COLUMN_LOCATIONID + " = " + id, null);
			break;
		}
		
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// if nothing is selected don't do anything
		
	}
	
	public int[] getProb(long id){
		System.out.println(id);
		int[] returnArray=new int[2];
		Cursor cursor=database.query(MySQLiteHelper.TABLE_LOCATION, locationPreferences,MySQLiteHelper.COLUMN_LOCATIONID + " = " + id,null,null,null,null);
		cursor.moveToFirst();
		returnArray[0]=cursor.getInt(0);
		returnArray[1]=cursor.getInt(1);
		cursor.close();
		return returnArray;
	}
	
	//must close and open the database at the correct times. 
		@Override
		protected void onStop(){
			super.onStop();
			database.close();
		}
				
		@Override
		protected void onStart(){
			super.onStart();
			database=helper.getWritableDatabase();
		}

}

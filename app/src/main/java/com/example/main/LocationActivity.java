package com.example.main;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class LocationActivity extends Activity implements OnItemSelectedListener{
	public static final String DEFAULT_PROB="DefaultProb";
	EditText editText;
	MySQLiteHelper helper;
	private SQLiteDatabase database;
	String profileNames[]=new String[]{MySQLiteHelper.COLUMN_LOCATIONPROFILE};
	LocationAdapter adapter;
	private ListView locationList;
	//check to see if google play is available
	boolean googlePlayAvailable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		editText=(EditText)findViewById(R.id.locationName);
		
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		
		
		//get all of the location profiles
		List<DeleteLV> locations=new ArrayList<DeleteLV>();
		locations=getAllLocations();
		
		//need to get the default probability for location
		SharedPreferences settings=getSharedPreferences(DEFAULT_PROB, 0);
		int defaultProb=settings.getInt("locationDefaultProb", 5);
		
		//populate the default probability spinner with 1 through 10
		Spinner defaultProbSpinner=(Spinner)findViewById(R.id.locationDefaultProb);
		
		ArrayAdapter<CharSequence> spinnerAdapter= ArrayAdapter.createFromResource(this, R.array.preference_spinner, 
				android.R.layout.simple_spinner_dropdown_item);
		
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		defaultProbSpinner.setAdapter(spinnerAdapter);
		defaultProbSpinner.setSelection(defaultProb);
		defaultProbSpinner.setOnItemSelectedListener(this);
		
		
		//Use the ArrayAdapter to show the elements in a ListView
		adapter= new LocationAdapter(this, R.layout.group_row_layout, locations);
		
		locationList=(ListView)findViewById(R.id.location_list);
		locationList.setAdapter(adapter);
		
		googlePlayAvailable=false;
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)==ConnectionResult.SUCCESS){
			googlePlayAvailable=true;
		}
		
		
		editText.setOnKeyListener(new View.OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN)
					if((keyCode==KeyEvent.KEYCODE_DPAD_CENTER) ||
					(keyCode==KeyEvent.KEYCODE_ENTER)){
			if(googlePlayAvailable){
			createLocation();
			}else{
				Toast.makeText(v.getContext(), "Please Download Google Play Services", Toast.LENGTH_LONG).show();
			}
			editText.setText("");
			
			return true;
		}
				return false;
			}
		});
						
}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}
	
	//gets all the locations
	 public List<DeleteLV> getAllLocations(){
			List<DeleteLV> locations=new ArrayList<DeleteLV>();
		 	Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION, profileNames, null, null, null, null, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				locations.add(new DeleteLV(cursor.getString(0), R.id.deleteBtn));
				cursor.moveToNext();
			}
			cursor.close();
			return locations;
	 }
	 
	 public void createLocation(){
		 	Boolean exists=false;
		 	
		 	DeleteLV newgroup = new DeleteLV(editText.getText().toString(), R.id.deleteBtn);
		    
		      //checks to make sure a group with this same name doesn't already exist
		      //if there are no groups created yet, set exist to false
		    if(adapter.getCount()==0){
		    	exists=false;
		    }
		    else{
		      for(int i=0; i<adapter.getCount(); i++){
		    	  if(adapter.getItem(i).title.equals(newgroup)){
		    		  exists=true;
		    		  break;
		    	  }
		      }
		    }
		      if(!exists){
		      adapter.add(newgroup);
		      insertGroup(newgroup.title);
		      }
		      else{
		    	  Toast.makeText(getApplicationContext(), "This Group Already Exists" , Toast.LENGTH_SHORT).show();
		      }
		    adapter.notifyDataSetChanged();
	    }
	 
	 public void insertGroup(String newgroup){
		 ContentValues values = new ContentValues();
		 values.put(MySQLiteHelper.COLUMN_LOCATIONPROFILE, newgroup);
			//sets the default preference to 5
			values.put(MySQLiteHelper.COLUMN_LOCATIONPROB, 5);
			//set the default call preference to ring
			values.put(MySQLiteHelper.COLUMN_LOCATIONINTERRUPTIONPREF,0);
			//set the latitude and longitude to 0 as default (that way if we try and get the values and they haven't been set they
			//won't return null
			values.put(MySQLiteHelper.COLUMN_LOCATIONLAT, 0);
			values.put(MySQLiteHelper.COLUMN_LOCATIONLONG, 0);
			//before any values are set for the location name, it acts for the user to please search for a location
			values.put(MySQLiteHelper.COLUMN_LOCATIONNAME, "No Location Set");
			database.insert(MySQLiteHelper.TABLE_LOCATION, null,
				        values);
	 }
	 
	 public void onItemSelected(AdapterView<?> parent, View view,
				int pos, long id){
			SharedPreferences preferences=getSharedPreferences(DEFAULT_PROB,0);
			SharedPreferences.Editor editor=preferences.edit();
			editor.putInt("locationDefaultProb",pos);
			editor.commit();
			
			}
		
		//handles when nothing has been selected
			public void onNothingSelected(AdapterView<?> parent){
				//another callback interface
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

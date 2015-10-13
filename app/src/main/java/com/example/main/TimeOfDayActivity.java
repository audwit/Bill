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

public class TimeOfDayActivity extends Activity implements OnItemSelectedListener {

	public static final String DEFAULT_PROB="DefaultProb";
	MySQLiteHelper helper;
	private SQLiteDatabase database;
	String profileNames[]=new String[]{MySQLiteHelper.COLUMN_TODTITLE};
	TODAdapter adapter; //should work here, even though it was initially intended for the contact activity
	private ListView todList;
	EditText editText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_of_day);
		
		
		//Instantiate my editText
		editText=(EditText)findViewById(R.id.newTODProfile);
		//get the database to save the individual profiles to
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		
		//get all of the time of day profiles
		List<DeleteLV> group=new ArrayList<DeleteLV>();
		group=getAllGroups();
		
		
		
		//need to get the default probability for time of day
		SharedPreferences settings=getSharedPreferences(DEFAULT_PROB, 0);
		int defaultProb=settings.getInt("timeOfDayDefaultProb", 5);
				
		//populate the default probability spinner with 1 through 10
		Spinner defaultProbSpinner=(Spinner)findViewById(R.id.timeOfDayDefaultProb);
						
		ArrayAdapter<CharSequence> spinnerAdapter= ArrayAdapter.createFromResource(this, R.array.preference_spinner, 
						android.R.layout.simple_spinner_dropdown_item);
					
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		defaultProbSpinner.setAdapter(spinnerAdapter);
		defaultProbSpinner.setSelection(defaultProb);
		defaultProbSpinner.setOnItemSelectedListener(this);
		
		//Use the ArrayAdapter to show the elements in the ListView
		adapter=new TODAdapter(this,R.layout.group_row_layout, group);
		
		todList=(ListView)findViewById(R.id.todList);
		todList.setAdapter(adapter);
		
		editText.setOnKeyListener(new View.OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN)
					if((keyCode==KeyEvent.KEYCODE_DPAD_CENTER) ||
					(keyCode==KeyEvent.KEYCODE_ENTER)){
			createGroup();
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
		getMenuInflater().inflate(R.menu.time_of_day, menu);
		return true;
	}
	
	public void onItemSelected(AdapterView<?> parent, View view,
			int pos, long id){
		SharedPreferences preferences=getSharedPreferences(DEFAULT_PROB,0);
		SharedPreferences.Editor editor=preferences.edit();
		editor.putInt("timeOfDayDefaultProb",pos);
		editor.commit();
		
		}
	
	//handles when nothing has been selected
		public void onNothingSelected(AdapterView<?> parent){
			//another callback interface
		}
		
		 public List<DeleteLV> getAllGroups(){
				List<DeleteLV> groups=new ArrayList<DeleteLV>();
			 	Cursor cursor = database.query(MySQLiteHelper.TABLE_TOD_PROFILE, profileNames, null, null, null, null, null);
				cursor.moveToFirst();
				while(!cursor.isAfterLast()){
					groups.add(new DeleteLV(cursor.getString(0), R.id.deleteBtn));
					cursor.moveToNext();
				}
				cursor.close();
				return groups;
		 }
		 
		 public void createGroup(){
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
			 values.put(MySQLiteHelper.COLUMN_TODTITLE, newgroup);
				//sets the default preference to 1
				values.put(MySQLiteHelper.COLUMN_TODPROB, 5);
				//set the default call preference to ring
				values.put(MySQLiteHelper.COLUMN_TODINTERRUPTIONPREF,0);
				//set all the days of the week to false
				values.put(MySQLiteHelper.COLUMN_MONDAY, 0);
				values.put(MySQLiteHelper.COLUMN_TUESDAY, 0);
				values.put(MySQLiteHelper.COLUMN_WEDNESDAY, 0);
				values.put(MySQLiteHelper.COLUMN_THURSDAY, 0);
				values.put(MySQLiteHelper.COLUMN_FRIDAY, 0);
				values.put(MySQLiteHelper.COLUMN_SATURDAY, 0);
				values.put(MySQLiteHelper.COLUMN_SUNDAY, 0);
				values.put(MySQLiteHelper.COLUMN_STARTTIME, 0);
				values.put(MySQLiteHelper.COLUMN_ENDTIME, 0);
				database.insert(MySQLiteHelper.TABLE_TOD_PROFILE, null,
					        values);
		 }
		 
		 
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

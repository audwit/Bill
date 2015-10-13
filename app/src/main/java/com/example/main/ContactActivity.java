package com.example.main;

//import java.util.List;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ContactActivity extends Activity implements OnItemSelectedListener{
	EditText editText;
	ListView layout;
	List<DeleteLV> group= new ArrayList<DeleteLV>();
	long sendId;
	static String sendKey="sendKey";
	static String sendProbKey="sendProb";
	static String sendCallPrefKey="sendCallPref";
	static String sendGroupKey="sendGroupName";
	static int sendProb;
	MySQLiteHelper helper;
	private SQLiteDatabase database;
	String allColumns[]=new String[]{MySQLiteHelper.COLUMN_GROUPNAME, MySQLiteHelper.COLUMN_GROUPPROB};
	String idAndProb[]=new String[]{MySQLiteHelper.COLUMN_GROUPID, MySQLiteHelper.COLUMN_GROUPPROB, MySQLiteHelper.COLUMN_GROUPNAME};
	String contactName[]=new String[]{MySQLiteHelper.COLUMN_CONTACTNAME};
	private ListView grouplist;
	GroupAdapter adapter;
	public static final String DEFAULT_PROB="DefaultProb";
	//the input method manager allows for closing the keyboard upon entering something
	InputMethodManager keyboard; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);
		editText=(EditText)findViewById(R.id.groupName);
		
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		
		
		group=getAllGroups();
		
		//need to get the default probability for contacts
		SharedPreferences settings=getSharedPreferences(DEFAULT_PROB, 0);
		int defaultProb=settings.getInt("contactDefaultProb", 5);
		
		//populate the default probability spinner with 1 through 10
		Spinner defaultProbSpinner=(Spinner)findViewById(R.id.contactDefaultProb);
		
		ArrayAdapter<CharSequence> spinnerAdapter= ArrayAdapter.createFromResource(this, R.array.preference_spinner, 
				android.R.layout.simple_spinner_dropdown_item);
		
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		defaultProbSpinner.setAdapter(spinnerAdapter);
		defaultProbSpinner.setSelection(defaultProb);
		defaultProbSpinner.setOnItemSelectedListener(this);
		
		
		//Use the ArrayAdapter to show the elements in a ListView
		adapter= new GroupAdapter(this, R.layout.group_row_layout, group);
		
		View header=(View)getLayoutInflater().inflate(R.layout.listview_header, null);
		
		
		
		grouplist=(ListView)findViewById(R.id.group_list);
		
		grouplist.addHeaderView(header);
		grouplist.setAdapter(adapter);
		
			editText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN)
					if((keyCode==KeyEvent.KEYCODE_DPAD_CENTER) ||
					(keyCode==KeyEvent.KEYCODE_ENTER)){
			//group.add(0, editText.getText().toString());
			createGroup();
			
			editText.setText("");
			//this should take the keyboard down
			
			return true;
		}
				return false;
			}
		});
		
			
		}
		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact, menu);
		return true;
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
	 

	 
	 
	 public List<DeleteLV> getAllGroups(){
			List<DeleteLV> groups=new ArrayList<DeleteLV>();
		 	Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPS, allColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				groups.add(new DeleteLV(cursor.getString(0), R.id.deleteBtn));
				cursor.moveToNext();
			}
			cursor.close();
			return groups;
	 }
	 
	 public void insertGroup(String newgroup){
		 ContentValues values = new ContentValues();
		 values.put(MySQLiteHelper.COLUMN_GROUPNAME, newgroup);
			//sets the default preference to 1
			values.put(MySQLiteHelper.COLUMN_GROUPPROB, 1);
			//set the default call preference to ring
			values.put(MySQLiteHelper.COLUMN_GROUPINTERRUPTIONPREF,0);
			database.insert(MySQLiteHelper.TABLE_GROUPS, null,
				        values);
	 }
	 
	/* @Override
	 protected void onListItemClick(ListView lv, View v, int position, long id){
		Intent i= new Intent(ContactActivity.this, ContactInfoActivity.class);
		String selectedGroup=group.get(position);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPS, idAndProb, MySQLiteHelper.COLUMN_GROUPNAME + " = '" + selectedGroup + "'", null, null, null, null);
		cursor.moveToFirst();
		/*while(!cursor.isAfterLast()){
			System.out.println(cursor.getString(2));
			cursor.moveToNext();
		}*/
		
		/*sendId=cursor.getLong(0);
		sendProb=cursor.getInt(1);
		i.putExtra(sendKey,sendId);
		i.putExtra(sendProbKey, sendProb);
		startActivity(i);
	 }*/
	 
	 
	 public void onClick(View v){
		 System.out.println(v.getId());
	 }
	 

	public void deleteGroup(View v){
	 
	 //gets the groupId of the first group in the list
	 Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPS, idAndProb, MySQLiteHelper.COLUMN_GROUPNAME + " = '" + adapter.getItem(0) + "'", null, null, null, null);
	 cursor.moveToFirst();
	 
	//delete all the contacts that have a contact groupId equal to the groupId
	 database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_CONTACTGROUP + " = " + cursor.getLong(0),null);
	 
	 //delete the first group
	 database.delete(MySQLiteHelper.TABLE_GROUPS, MySQLiteHelper.COLUMN_GROUPID + " = " + cursor.getLong(0),null);
	 
	 //remove the group from the adapter
	 adapter.remove(adapter.getItem(0));
	 
	 //notify the adapter that a view has been removed so it can recalculate
	 adapter.notifyDataSetChanged();
	 
	 //a test to see the contacts that are still in the database to make sure that they get deleted with the group so they do not take up unnecessary space
	 cursor=database.query(MySQLiteHelper.TABLE_CONTACTS, contactName,null, null, null, null, null);
	 cursor.moveToFirst();
	 while(!cursor.isAfterLast()){
		 System.out.println(cursor.getString(0));
		 cursor.moveToNext();
	 }
	 cursor.close();
	 
 }
	
	public void onItemSelected(AdapterView<?> parent, View view,
			int pos, long id){
		SharedPreferences preferences=getSharedPreferences(DEFAULT_PROB,0);
		SharedPreferences.Editor editor=preferences.edit();
		editor.putInt("contactDefaultProb",pos);
		editor.commit();
		
		}
	
	//handles when nothing has been selected
		public void onNothingSelected(AdapterView<?> parent){
			//another callback interface
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

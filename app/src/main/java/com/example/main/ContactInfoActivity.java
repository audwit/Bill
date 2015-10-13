package com.example.main;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



public class ContactInfoActivity extends Activity implements OnItemSelectedListener {
	long groupId;
	int prob;
	int callPref;
	String groupName;
	static final int PICK_CONTACT_REQUEST=1; //the request code
	//get the database that will be used
	MySQLiteHelper helper;
	private SQLiteDatabase database;
	private String[] contactinfo={MySQLiteHelper.COLUMN_CONTACTNAME, MySQLiteHelper.COLUMN_CONTACTPHONE};//i don't think the phone is necessary
	List<DeleteLV> contactNames=new ArrayList<DeleteLV>();
	ContactAdapter contactAdapter;
	private ListView contactlist;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_info);
		
		//instantiate the database
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		
		//shared preferences to get the correct groups
		groupId=getIntent().getLongExtra(ContactActivity.sendKey, 0);
		prob=getIntent().getIntExtra(ContactActivity.sendProbKey, 0);
		callPref=getIntent().getIntExtra(ContactActivity.sendCallPrefKey,0);
		groupName=getIntent().getStringExtra(ContactActivity.sendGroupKey);
		
		//set the text at the top of the activity that indicates which group you are currently adding to
		TextView group=(TextView) findViewById(R.id.groupName);
		group.setText("Group: " + groupName.replace("''", "'"));
		
		//no options exist yet, but an array list called contactNames will be created in anticipation of such an event
        contactNames=getAllContacts(groupId);
		
        //create the contactAdapter
        contactAdapter=new ContactAdapter(this, R.layout.group_row_layout,contactNames);
		
        contactlist=(ListView)findViewById(R.id.contactlist);
        contactlist.setAdapter(contactAdapter);
	
		Spinner spinner=(Spinner)findViewById(R.id.preference_spinner);
		
		Spinner callspinner=(Spinner)findViewById(R.id.call_preference_spinner);
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
		
	}
	
	//handles clicking a spinner option
	//for some reason calls this on launch,
	public void onItemSelected(AdapterView<?> parent, View view,
		int pos, long id){
		if(!database.isOpen()){
			database=helper.getWritableDatabase();
		}
		switch(parent.getId()){
		case R.id.preference_spinner:
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_GROUPPROB, pos);
			database.update(MySQLiteHelper.TABLE_GROUPS, values, MySQLiteHelper.COLUMN_GROUPID + " = " + groupId, null);
			break;
		case R.id.call_preference_spinner:
			ContentValues callvalues = new ContentValues();
			callvalues.put(MySQLiteHelper.COLUMN_GROUPINTERRUPTIONPREF, pos);
			database.update(MySQLiteHelper.TABLE_GROUPS, callvalues, MySQLiteHelper.COLUMN_GROUPID + " = " + groupId, null);
			break;
		}
		database.close();
		
	}
	
	//handles when nothing has been selected
	public void onNothingSelected(AdapterView<?> parent){
		//another callback interface
	}

	
	//when the user clicks add contact, this will start the contact activity, selecting a contact then returns the contact
	public void getContact(View v){
		Intent getContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
		getContactIntent.setType(Phone.CONTENT_TYPE); //shows only contacts with phone numbers
		startActivityForResult(getContactIntent, PICK_CONTACT_REQUEST); 
	}
	
	
	//this is where we get the result from the selected contact
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(!database.isOpen()){
			database=helper.getWritableDatabase();
		}
		//check to see which request we're responding to
		if(requestCode==PICK_CONTACT_REQUEST){
			//make sure the request was successful
			if(resultCode==RESULT_OK){
				//the follow code is not the best for it can lead to long load times. It suggests using a cursorloader and such
				//get the uri that points to the selected contact
				Uri contactUri=data.getData();
				//query for name and number
				String[] projection = {Phone.DISPLAY_NAME, Phone.NUMBER};
				//this is apparently not the best way, go here for reasons http://developer.android.com/training/basics/intents/result.html
				Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
				cursor.moveToFirst();
				
				//Retrieve the name from the name column
				int columnname = cursor.getColumnIndex(Phone.DISPLAY_NAME);
				int columnnumber=cursor.getColumnIndex(Phone.NUMBER);
				
				DeleteLV name=new DeleteLV(cursor.getString(columnname),R.id.deleteBtn);
				System.out.println(name.title);
				String phonenumber=cursor.getString(columnnumber);
				
				//need to check to see if the contact is already in the db, sending back the name of group that contact is i
				
				if(!contactExistAlready(name.title)){
				contactAdapter.add(name);
				contactAdapter.notifyDataSetChanged();
				//add the information to the database
				insertContactInfo(name.title, phonenumber, groupId);
				}
				
				cursor.close();
			}
		}
		
		database.close();
	}
	
	//method to determine whether or not the contact is already in the database
	private boolean contactExistAlready(String name){
		if(!database.isOpen()){
			database=helper.getWritableDatabase();
		}
		Cursor cursor = database.rawQuery("Select " + MySQLiteHelper.COLUMN_GROUPNAME + " from " 
				+ MySQLiteHelper.TABLE_GROUPS + ", " + MySQLiteHelper.TABLE_CONTACTS + " where " +
						MySQLiteHelper.COLUMN_CONTACTGROUP + " = " + MySQLiteHelper.COLUMN_GROUPID + " and " 
						+ MySQLiteHelper.COLUMN_CONTACTNAME + " = '" + name + "'",null);
		cursor.moveToFirst();
		//if the contact isn't already in a group, give the go ahead
		if(cursor.getCount()==0){
			cursor.close();
			database.close();
			return false;
		}
		else{
			Toast.makeText(this, name+ " is already in the group " + cursor.getString(0), Toast.LENGTH_LONG).show();
			cursor.close();
			database.close();
			return true;
		}
		
		
	}
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_info, menu);
		return true;
		
	}
	
	
	public List<DeleteLV> getAllContacts(long groupId){
		if(!database.isOpen()){
			database=helper.getWritableDatabase();
		}
		List<DeleteLV> contacts=new ArrayList<DeleteLV>();
		Cursor cursor=database.query(MySQLiteHelper.TABLE_CONTACTS, contactinfo,MySQLiteHelper.COLUMN_CONTACTGROUP+ " = " + groupId,null,null,null,null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			contacts.add(new DeleteLV(cursor.getString(0), R.id.deleteBtn));
			cursor.moveToNext();
		}
		cursor.close();
		database.close();
		return contacts;
	
	}
	
	
	
	public void insertContactInfo(String name, String number, long id){
		if(!database.isOpen()){
			database=helper.getWritableDatabase();
		}
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_CONTACTNAME, name);
		values.put(MySQLiteHelper.COLUMN_CONTACTPHONE, number);
		values.put(MySQLiteHelper.COLUMN_CONTACTGROUP, id);
		database.insert(MySQLiteHelper.TABLE_CONTACTS, null, values);
		database.close();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		database.close();
	}
	
	@Override 
	protected void onStart(){
		super.onStart();
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		
	}

}
	
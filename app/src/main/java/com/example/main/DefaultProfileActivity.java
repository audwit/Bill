package com.example.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class DefaultProfileActivity extends Activity implements OnItemSelectedListener{

	public static final String DEFAULT_PHONE_SETTING="defaultPhoneSetting";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_profile);
		
		//Restore shaved preferences
		SharedPreferences phoneProfileSettings=getSharedPreferences(DEFAULT_PHONE_SETTING,0);
		int defaultPhonePref=phoneProfileSettings.getInt("defaultPhonePref", 1);
		
		
		Spinner defaultCallSpinner=(Spinner)findViewById(R.id.defaultCallPreference);
		
		//Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this, R.array.default_call_preference_spinner, 
				android.R.layout.simple_spinner_dropdown_item);
				
		//Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		defaultCallSpinner.setAdapter(adapter);
		defaultCallSpinner.setSelection(defaultPhonePref);
		//attach a selection listener to it to save the preference
		defaultCallSpinner.setOnItemSelectedListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.default_profile, menu);
		return true;
	}
	
	public void onItemSelected(AdapterView<?> parent, View view,
			int pos, long id){
	
		SharedPreferences preferences=getSharedPreferences(DEFAULT_PHONE_SETTING,0);
		SharedPreferences.Editor editor=preferences.edit();
		
		editor.putInt("defaultPhonePref",pos);
		editor.commit();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}

package com.example.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

public class WeightsActivity extends Activity {
	Weight weight_data[]=new Weight[5];
	private ListView listView1;
	WeightAdapter adapter;
	
	//five shared preferences for the five contexts, this will set up the sharedPreferences
	
	//first we need a string that will work as an identifier
	public static final String WEIGHTS="WeightPreferences";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weights);
		
		//the weights will need to get the preferences saved from before, unless it is the first time, in which a default
		//order is set. 
		SharedPreferences preferences=getSharedPreferences(WEIGHTS,0);
		
		//the order is unkown going into this, so the only approach is to instantiate all 5 using either the set values, or the default.
		//if two are transposed, each is saved to a new place, so there should be no issue with duplicates
		
		//assigning the weights, either default or from saved preferences (this should be required by the user before it starts working)
		weight_data[preferences.getInt("Location", 0)]=new Weight("Location", R.id.ubutton, R.id.dbutton);
		weight_data[preferences.getInt("Schedule", 1)]=new Weight("Schedule", R.id.ubutton, R.id.dbutton);
		weight_data[preferences.getInt("Contact", 2)]=new Weight("Contact", R.id.ubutton, R.id.dbutton);
		weight_data[preferences.getInt("Time Of Day", 3)]=new Weight("Time Of Day", R.id.ubutton, R.id.dbutton);
		weight_data[preferences.getInt("Driving", 4)]=new Weight("Driving", R.id.ubutton, R.id.dbutton);
		
		
		adapter=new WeightAdapter(WeightsActivity.this, R.layout.row_layout, weight_data);
		listView1=(ListView)findViewById(R.id.Weight_ListView);
		listView1.setAdapter(adapter);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
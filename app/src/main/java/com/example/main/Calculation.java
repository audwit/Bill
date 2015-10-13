package com.example.main;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;

public class Calculation extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{
	/*these four arrays hold the important information about each context. Each index is a context and this is the structure
	0 Location
	1 Schedule
	2 Contact
	3 Time of Day
	4 Driving
	
	*/
	
	//the number of contexts we are currently evaluating, though it can be improved.
	private static final int NUMBER_OF_CONTEXTS=5;
	
	//whether or not a default value is assigned
	boolean[] situations=new boolean[NUMBER_OF_CONTEXTS];
	
	//the probability associated with each one
	double[] probabilities=new double[NUMBER_OF_CONTEXTS];
	
	//the weights associated with each contexts
	double[] weights=new double[NUMBER_OF_CONTEXTS];
	
	//the interruption preference ("Ring or Vibrate")
	int[] interruptionPref=new int[NUMBER_OF_CONTEXTS];
	
	//Keys for the shared preferences
	public static final String WEIGHTS="WeightPreferences";
	public static final String DEFAULT_PROB="DefaultProb";
	public static final String DRIVING_SETTINGS="DriverSettings";
	public static final String DEFAULT_PHONE_SETTING="defaultPhoneSetting";
	public static final String DEFAULT_CALL_PREF="defaultCallPref";
	
	//the sum of the weights of all the contexts
	public static final double CONTEXT_SUM=15.;
	
	//the P(I) we will evaluate
	static double probability=0;
	
	//this class allows for the manipulation of the interruption profile
	AudioManager am;
	
	//only used for displaying the current mode 
	String mode="default";
	MySQLiteHelper helper;
	private SQLiteDatabase database;
	
	//a issue has arisen and this variable looks to fix the problem, though it is not the best way to do so.
	private int scheduleCount;
	private int todCount;
	
	//location services
	LocationClient mLocationClient;
	
	//the columns to be return in regards to the location service
	String locationItems[]=new String[]{MySQLiteHelper.COLUMN_LOCATIONLAT,MySQLiteHelper.COLUMN_LOCATIONLONG,
			MySQLiteHelper.COLUMN_LOCATIONPROB, MySQLiteHelper.COLUMN_LOCATIONINTERRUPTIONPREF};
	
	//the shared preferences
	SharedPreferences defaultProb;
	SharedPreferences contextWeights;
	SharedPreferences drivingPreferences;
	SharedPreferences scheduleMOI;
	
	//Calendar
	//Calendar cal=Calendar.getInstance();
	
	//this is for timing issues
	long start;
	
	@Override
	public void onCreate(){
		super.onCreate();
		am=(AudioManager) getSystemService(Service.AUDIO_SERVICE);
		
		//this database connect is instantiated to access the user preference information
		helper=new MySQLiteHelper(this);
		database=helper.getWritableDatabase();
		mLocationClient=new LocationClient(this,this,this);
		defaultProb=getSharedPreferences(DEFAULT_PROB,0);
		contextWeights=getSharedPreferences(WEIGHTS,0);
		drivingPreferences=getSharedPreferences(DRIVING_SETTINGS,0);
		scheduleMOI=getSharedPreferences(DEFAULT_CALL_PREF,0);
	}
	
	@Override
	public IBinder onBind(Intent intent){
		//Replace with service binding implementation
		return null;
	}
	
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(!database.isOpen()){
			helper=new MySQLiteHelper(this);
			database=helper.getWritableDatabase();
		}
		start=intent.getLongExtra("startTime", 0);
		//will need default method of interruption (Ring, vibrate, silent) in case it doesn't hit any of them (unless the default takes care of that)
		
		
		//set the starting preferences
		setStartingValues();
		
		//Retrieves information about the driving context
		setDrivingValues();
		
		//Retrieves the information about the schedule context
		setScheduleValues();
		
		//Retrieves the information about the contact context
		setContactValues(intent);
		
		//Retrieves the information about the time of day context
		setTODValues();
		
		//now is the time to get the location context up and running
		mLocationClient.connect();
		
		//as I was not able to evaluate correctly before because it evaluated before it was connected, I will now evaluate after
		//either connecting or having a connection failure (hopefully this will be quick enough, otherwise I'll have to go about this 
		//differently)
		database.close();
		stopSelf();
		return Service.START_NOT_STICKY;
	}
	
	private double[] getScheduleInfo(String str) {
		double[] returnArray=new double[2];
		//StringTokenizer st=new StringTokenizer(str,"$");
		Pattern initial=Pattern.compile("\\$[0-9].[0-9].*\\$");
		Pattern findDouble=Pattern.compile("[0-9].[0-9]");
		Pattern findMethod=Pattern.compile("[vV,rR]");
		Matcher matcher;
		String initialString="";
		//String information=st.nextElement().toString();
		matcher=initial.matcher(str);
		if(matcher.find()){
			initialString=matcher.group();
		}
		//System.out.println(initialString);
		matcher=findDouble.matcher(initialString);
		if(matcher.find()){
			//System.out.println("Sched Group: " + matcher.group());
			returnArray[0]=Double.parseDouble(matcher.group());
		}	
		else{
			returnArray[0]=-1.0;
		}
		matcher=findMethod.matcher(initialString);
		if(matcher.find()){
			if(matcher.group().equals("R") || matcher.group().equals("r")){
			returnArray[1]=0.0;
		}
		else{
			returnArray[1]=1.0;
		}
	
		}
		return returnArray;
	}
	private double[] getTimeOfDayInfo(String str) {
		double[] returnArray=new double[2];
		//StringTokenizer st=new StringTokenizer(str,"&");
		Pattern initial=Pattern.compile("\\&[0-9].[0-9]\\s*[vV,rR]\\&");
		Pattern findDouble=Pattern.compile("[0-9].[0-9]");
		Pattern findMethod=Pattern.compile("[vV,rR]");
		Matcher matcher;
		String initialString="";
		//String information=st.nextElement().toString();
		matcher=initial.matcher(str);
		if(matcher.find()){
			initialString=matcher.group();
		}
		matcher=findDouble.matcher(initialString);
		if(matcher.find()){
			//System.out.print("Sched Group: " + matcher.group());
			returnArray[0]=Double.parseDouble(matcher.group());
		}	
		else{
			returnArray[0]=-1.0;
		}
		matcher=findMethod.matcher(str);
		if(matcher.find()){
			if(matcher.group()=="V" || matcher.group()=="v" ){
			returnArray[1]=0.0;
		}
		else{
			returnArray[1]=1.0;
		}
	
		}
		return returnArray;
	}

	public static void evaluate(boolean[] situations, double[] probabilities, double[] weights){
		//probability must be returned to 0 each time
		probability=0;
		String display="";
		//double numerator=0;
		//double denominator=0;
		
		//not currently re-adjusting weights, but still dividing by them. It won't make a difference if they sum to one. 
		for(int i=0; i<situations.length; i++){	
				probability+=(probabilities[i]*weights[i]);	
				display+=probabilities[i] + "*" + weights[i] + "+";
				//System.out.println("W = " + weights[i] + " P(I) = " + probabilities[i]);
		}
		
		//System.out.println("This is the function evaluated : " + display);
		//System.out.println("This is the probability " + probability);	
	}
	
	//used to get the appropriate value to rank the weights
	public static int rankWeight(int i){
		switch(i){
		case 1:
			return 5;
		case 2:
			return 4;
		case 3:
			return 3;
		case 4:
			return 2;
		case 5:
			return 1;
		}
		return 0;
	}
	
	//this will adjust the weights so those that are actually measured have a higher priority than those that are not measured. 
	public void adjustWeights(){
		//array for rank  manipulation
		int[] adjustrank=new int[NUMBER_OF_CONTEXTS];
		
		for(int i=0; i<NUMBER_OF_CONTEXTS; i++){
			adjustrank[i]=0;
			if(situations[i]){
					for(int j=0; j<NUMBER_OF_CONTEXTS; j++){
						if(weights[i]<=weights[j]&&!situations[j]){
							adjustrank[i]++;
						}
					}
			}
			if(!situations[i]){
				for(int j=0; j<NUMBER_OF_CONTEXTS; j++){
					if(weights[i]>=weights[j]&&situations[j]){
						adjustrank[i]--;
				}
			}
			}
				
		}
		//after getting what each should be re-weighted too, apply those weights
		for(int i=0; i<NUMBER_OF_CONTEXTS; i++){
		weights[i]=((weights[i]*CONTEXT_SUM)+adjustrank[i])/CONTEXT_SUM;
		}
	}
	
	public String getDayOfWeek(int day){
		switch(day){
		case 1:
			return MySQLiteHelper.COLUMN_SUNDAY;
		case 2:
			return MySQLiteHelper.COLUMN_MONDAY;
		case 3:
			return MySQLiteHelper.COLUMN_TUESDAY;
		case 4:
			return MySQLiteHelper.COLUMN_WEDNESDAY;
		case 5:
			return MySQLiteHelper.COLUMN_THURSDAY;
		case 6:
			return MySQLiteHelper.COLUMN_FRIDAY;
		case 7:
			return MySQLiteHelper.COLUMN_SATURDAY;
		default:
			return null;
		}
	}
	
	//this method will set all of the starting values for the contexts
	public void setStartingValues(){
		//get the shared preferences for the default probabilities and weights
				//assign the default probabilities, if none set then it sets it as 5. 
				//must divide by 10 as they are in increments of 0.1, not 1.
				probabilities[0]=defaultProb.getInt("locationDefaultProb", 5)/10.;
				probabilities[1]=defaultProb.getInt("scheduleDefaultProb", 5)/10.;
				probabilities[2]=defaultProb.getInt("contactDefaultProb", 5)/10.;
				probabilities[3]=defaultProb.getInt("timeOfDayDefaultProb", 5)/10.;
				probabilities[4]=defaultProb.getInt("drivingDefaultProb", 5)/10.;
				
				
				//the weight assigned to contacts, if they aren't set yet give a default values in the order they will initially appear
				weights[0]=(double)rankWeight((contextWeights.getInt("Location", 0)+1))/CONTEXT_SUM;
				weights[1]=(double)rankWeight((contextWeights.getInt("Schedule", 1)+1))/CONTEXT_SUM;
				weights[2]=(double)rankWeight((contextWeights.getInt("Contact", 2)+1))/CONTEXT_SUM;
				weights[3]=(double)rankWeight((contextWeights.getInt("Time Of Day", 3)+1))/CONTEXT_SUM;
				weights[4]=(double)rankWeight((contextWeights.getInt("Driving", 4)+1))/CONTEXT_SUM;
				
				
				//initially set all the situations to false, but can be changed later. 
				for(int i=0; i<NUMBER_OF_CONTEXTS;i++){
					situations[i]=false;
				}
	}
	
	//sets the driving contexts values
	public void setDrivingValues(){
		//This section will determine whether or not to evaluate driving, and if so to set a probability
				situations[4]=drivingPreferences.getBoolean("evaluateDriving", false);
				if(situations[4]){
					probabilities[4]=drivingPreferences.getInt("mainDrivingProb", 5)/10.;
					interruptionPref[4]=drivingPreferences.getInt("callPreference",0);
				}
	}
	
	//sets the schedule contexts values
	public void setScheduleValues(){
		//This code will get the information necessary for schedule and time of day
				String projection[]={CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND};
				
				Long currentTime=Calendar.getInstance().getTimeInMillis();
				
				//Must specify a range to search for the event instances. I will try using simply the same start and stop, which will be the current time
				Uri.Builder builder=Instances.CONTENT_URI.buildUpon();
				ContentUris.appendId(builder, currentTime);
				ContentUris.appendId(builder, currentTime);
			
				
				
				//Get a cursor over the Events Provider, only getting current ones to filter the results
				Cursor eventCursor=getContentResolver().query(builder.build(), projection, null, null, null);
				
				eventCursor.moveToFirst();		
				
				if(eventCursor.getCount()>=1){
					//first, set the defaultEventProb, since an event did occur
					probabilities[1]=defaultProb.getInt("scheduleEventDefaultProb", 5)/10.;
					//as nothing will happen unless we evaluate this, we set the situations[1] to true
					situations[1]=true;
					//next, we need to get the preferred method of interruption
					interruptionPref[1]=scheduleMOI.getInt("scheduleDefaultCallPref", 0);
					
					scheduleCount=0;
					todCount=0;
					while(!eventCursor.isAfterLast()){
						//this means the user could enter a double that is not a incremented by 0.1
						double scheduleArray[]=getScheduleInfo(eventCursor.getString(0));
						double schedulePI=scheduleArray[0];
						int scheduleIntPref=(int)scheduleArray[1];
						if(schedulePI>=0 && schedulePI<=1.0){
							situations[1]=true;
							//in case of two or more events that have a probability and are schedule at the same time
							//it sets the probabilities to the lowest one. 
							if(scheduleCount==0){
							probabilities[1]=schedulePI;
							interruptionPref[1]=scheduleIntPref;
							}
							else{
								if(schedulePI<probabilities[1]){
									probabilities[1]=schedulePI;
								}
								if(scheduleIntPref>interruptionPref[1]){
									interruptionPref[1]=scheduleIntPref;
								}
							}
							scheduleCount++;
						}
						
						eventCursor.moveToNext();
					}
				}
				eventCursor.close();
	}
	
	//set contact context values
	public void setContactValues(Intent intent){
		//I don't think the format is necessary, but it doesn't hurt anything.
				String phoneNumber=PhoneNumberUtils.formatNumber(intent.getStringExtra("phoneNumber"));
				
				//Cursor will find if the contact has any preference. If not found, will be set to false in the model.
				if(!database.isOpen()){
					database=helper.getWritableDatabase();
				}
				
				Cursor cursor = database.rawQuery("Select " + MySQLiteHelper.COLUMN_GROUPPROB + "," + MySQLiteHelper.COLUMN_CONTACTPHONE + ", "
				+ MySQLiteHelper.COLUMN_GROUPINTERRUPTIONPREF +" from " 
				+ MySQLiteHelper.TABLE_GROUPS + "," + MySQLiteHelper.TABLE_CONTACTS + " where " +
						MySQLiteHelper.COLUMN_CONTACTGROUP + " = " + MySQLiteHelper.COLUMN_GROUPID,null);
				

				
				cursor.moveToFirst();
				while(!cursor.isAfterLast()){
					//compares the two strings so they don't have to be identical
					if(PhoneNumberUtils.compare(cursor.getString(1),phoneNumber)){
						situations[2]=true;
						probabilities[2]=((double)cursor.getInt(0))/10.;
						interruptionPref[2]=cursor.getInt(2);
						break;
					}
					else{
						cursor.moveToNext();
					}
				}
				cursor.close();
	}
	
	//set the time of day context values
	public void setTODValues(){
		//this section will focus on getting the time of day information
		//this array will look for the P(I) of the TOD and the preferred interruption
		Calendar cal=Calendar.getInstance();
		String[] todItems=new String[]{MySQLiteHelper.COLUMN_TODPROB, MySQLiteHelper.COLUMN_TODINTERRUPTIONPREF};
		//System.out.println(getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)));
		int specialTimeFormat=cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
		Cursor todCursor=database.query(MySQLiteHelper.TABLE_TOD_PROFILE, todItems, getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)) + " = 1"
				+ " and " + MySQLiteHelper.COLUMN_STARTTIME + " < '" + specialTimeFormat + "' and " + MySQLiteHelper.COLUMN_ENDTIME 
				+ " > '" + specialTimeFormat + "'", null, null, null, null); 
		todCursor.moveToFirst();
		if(todCursor.getCount()>0){
			//System.out.println("I got here from time of day");
			probabilities[3]=((double)todCursor.getInt(0))/10.;
			interruptionPref[3]=todCursor.getInt(1);
			situations[3]=true;
			}
		todCursor.close();
	}
	
	public void setLocationValues(){
			Location mCurrentLocation=mLocationClient.getLastLocation();
			//will give it up to a second to decide whether or not it can get the location.
			/*long startGettingLocation=System.nanoTime();
			double breakPoint=0.4;
			while(mCurrentLocation==null){
				if(((System.nanoTime()-startGettingLocation)/1000000000.)>=breakPoint){
					System.out.println("No location");
					break;
				}
				mCurrentLocation=mLocationClient.getLastLocation();
				
			}
			long gotLocation=System.nanoTime();
			System.out.println("Time to Get Location Values :" + ((gotLocation-startGettingLocation)/1000000000.));*/
			if(mCurrentLocation==null){
				//System.out.println("The location is null, will not evaluate this context");
				//as everything is already set to false, there is not reason to change anything else.
			}else{
			//will need to get all the rows from the location table, then iterate through to determine which are relevant
			Cursor locationCursor=database.query(MySQLiteHelper.TABLE_LOCATION, locationItems,null,null,null,null,null);
			locationCursor.moveToFirst();
			Location destination=new Location("Destination");
			while(!locationCursor.isAfterLast()){
				destination.setLatitude(locationCursor.getDouble(0));
				destination.setLongitude(locationCursor.getDouble(1));
				//System.out.println("Accuracy: " + mCurrentLocation.getAccuracy());
				if(mCurrentLocation.distanceTo(destination)<mCurrentLocation.getAccuracy()){
					//System.out.println("You are close by, about " + mCurrentLocation.distanceTo(destination) + " meters away");
					probabilities[0]=((double)locationCursor.getInt(2))/10.;
					interruptionPref[0]=locationCursor.getInt(3);
					situations[0]=true;
					break;
				}
				else{
				//System.out.println("You aren't so close by, about " + mCurrentLocation.distanceTo(destination) + " meters away");
				}
			
				locationCursor.moveToNext();
			}
			//System.out.println();
			locationCursor.close();
			}
			
	}
	//setRinger
	public void setRinger(){
		//this is a test to see what happens if I wait a while (emulating a long calculation time) to change the profile.
		//I predict the call will go throw with the last profile setting, and then change after the alloted time is up
		
		//get the shared preferences for defaultProfile
		SharedPreferences phoneProfileSettings=getSharedPreferences(DEFAULT_PHONE_SETTING,0);
		int defaultPhonePref=phoneProfileSettings.getInt("defaultPhonePref", 1);
		
		//used to determine which interruption method to use. 
		
		//first must decide if any context was measured
		boolean defaultProfile=true;
		for(int i=0;i<NUMBER_OF_CONTEXTS;i++){
			if(situations[i]==true){
				defaultProfile=false;
			}
		}
		if(!defaultProfile){
				int prefSum=0;
				if(probability>0.5){//prob above 0.5, set to correct interruption method. 
					for(int i=0; i<NUMBER_OF_CONTEXTS; i++){
						if(situations[i]==true){
							prefSum+=interruptionPref[i];
							//System.out.println(i + " " + interruptionPref[i]);
						}
					}
					if(prefSum==0){
						am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					}
					else{
						am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					}
				}
				else{
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				}
				
				//currently only used to display the mode, helpful for emulator
				if(am.getRingerMode()==AudioManager.RINGER_MODE_NORMAL){
				mode="Normal";
				}else if(am.getRingerMode()==AudioManager.RINGER_MODE_SILENT){
					mode="silent";
				}
				else if(am.getRingerMode()==AudioManager.RINGER_MODE_VIBRATE){
					mode="vibrate";
				}
				Toast.makeText(this, mode, Toast.LENGTH_LONG).show();
			
			}else{
				//System.out.println("When to the default as no contexts were hit");
				am.setRingerMode(defaultPhonePref);
			}
		long end=System.nanoTime();
		Log.i("time to run","Overall Time to Run: " + ((end-start)/1000000000.));
				//Don't know if this will have any negative effects, but thought I'd kill the service after it's done it's job. However, it  hasn't returned
				//Start_not_sticky yet, so I don't know.
	}
	
	//all the location services methods
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "Connect Failed", Toast.LENGTH_SHORT).show();	
		//setLocationValues();
		//right before evaluating we adjust the weights to give higher significants to those that are measurable
		adjustWeights();
		//this method evaluates the contexts using the weights, the probabilities, and whether or not the context is measurable 
		evaluate(situations, probabilities, weights);
		setRinger();
		mLocationClient.disconnect();
		
		// Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		// mLocationClient.requestLocationUpdates(mLocationRequest, this,null);
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		if(!database.isOpen()){
			helper=new MySQLiteHelper(this);
			database=helper.getWritableDatabase();
		}
		//System.out.println("Connected");
		setLocationValues();
		//right before evaluating we adjust the weights to give higher significants to those that are measurable
		adjustWeights();
		//this method evaluates the contexts using the weights, the probabilities, and whether or not the context is measurable 
		evaluate(situations, probabilities, weights);
		setRinger();
		mLocationClient.disconnect();
		database.close();
		// Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		// mLocationClient.requestLocationUpdates(mLocationRequest, this,null);
		
	}

	@Override
	public void onDisconnected() {
		 Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
		
	}
	
	//Define the callback method that receives location updates
	@Override
	public void onLocationChanged(Location location){
	}
	
	
}

/*try{
					while(!mLocationClient.isConnected()){
						Thread.sleep(200);
						breakPoint++;
						if(breakPoint>3){
							System.out.println("Could not connect :(");
							break;
						}
					}
				}
			catch (Exception e){
				System.out.println("I got the the exception");
			}
				if(mLocationClient.isConnected()){
					System.out.println("I'm connected");
				}*/


/*Thread t=new Thread(){
	int breakPoint=0;
	@Override
	public void run(){
		System.out.println("I got to the new thread");
		LocationGetter getLocation=new LocationGetter();
		Location currentLocation=getLocation.getLocation();
		if(currentLocation==null){
			System.out.println("The location is null, will not evaluate this context");
			//as everything is already set to false, there is not reason to change anything else.
		}else{
			Cursor locationCursor=database.query(MySQLiteHelper.TABLE_LOCATION, locationItems,null,null,null,null,null);
			locationCursor.moveToFirst();
			Location destination=new Location("Destination");
		while(!locationCursor.isAfterLast()){
			destination.setLatitude(locationCursor.getDouble(0));
			destination.setLongitude(locationCursor.getDouble(1));
			if(currentLocation.distanceTo(destination)<500){
				probabilities[0]=((double)locationCursor.getInt(2))/10.;
				interruptionPref[0]=locationCursor.getInt(2);
				situations[0]=true;
				break;
			}
		}
		}
		
	}

	};
	t.start();

	try {
	Thread.sleep(2000);
	} catch (InterruptedException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
	}*/


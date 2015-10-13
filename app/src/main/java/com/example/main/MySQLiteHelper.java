package com.example.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper{
	
	//table name and fields for the groups table
	public static final String TABLE_GROUPS="groups";
	public static final String COLUMN_GROUPID="group_id";
	public static final String COLUMN_GROUPNAME="group_name";
	public static final String COLUMN_GROUPPROB="groupprob";
	public static final String COLUMN_GROUPINTERRUPTIONPREF="interruptionPref";
	public static final String COLUMN_GROUPNONINTERRUPTIONPREF="noninterruptionPref";

	//table name and fields for the group_contacts table
	public static final String TABLE_CONTACTS="contacts";
	public static final String COLUMN_CONTACTSID="contacts_id";
	public static final String COLUMN_CONTACTNAME="contact_name";
	public static final String COLUMN_CONTACTPHONE="contact_phone";
	public static final String COLUMN_CONTACTGROUP="contact_group";
	
	//table name and fields for the TOD_profile table
	public static final String TABLE_TOD_PROFILE="timeOfDay";
	public static final String COLUMN_TODTITLE="tod_title";
	public static final String COLUMN_TODID="tod_id";
	public static final String COLUMN_TODPROB="tod_prob";
	public static final String COLUMN_TODINTERRUPTIONPREF="todInterruptionPref";
	public static final String COLUMN_TODNONINTERRUPTIONPREF="todNonInterruptionPref";
	public static final String COLUMN_STARTTIME="startTime";
	public static final String COLUMN_ENDTIME="endTime";
	public static final String COLUMN_MONDAY="monday";
	public static final String COLUMN_TUESDAY="tuesday";
	public static final String COLUMN_WEDNESDAY="wednesday";
	public static final String COLUMN_THURSDAY="thursday";
	public static final String COLUMN_FRIDAY="friday";
	public static final String COLUMN_SATURDAY="saturday";
	public static final String COLUMN_SUNDAY="sunday";
	
	//table name and fields for the location table
	public static final String TABLE_LOCATION="location";
	public static final String COLUMN_LOCATIONID="location_id";
	public static final String COLUMN_LOCATIONPROFILE="location_profile";
	public static final String COLUMN_LOCATIONNAME="location_name";
	public static final String COLUMN_LOCATIONPROB="location_prob";
	public static final String COLUMN_LOCATIONINTERRUPTIONPREF="locationInterruptionPref";
	public static final String COLUMN_LOCATIONNONINTERRUPTIONPREF="locationNonInterruptionPref";
	public static final String COLUMN_LOCATIONLAT="locationlat";
	public static final String COLUMN_LOCATIONLONG="locationlong";

	
	
	private static final String DATABASE_NAME = "interruption.db";
	private static final int DATABASE_VERSION = 9;  //what happens if you roll back?
	
	//Creates the groups table
	private static final String DATABASE_GROUPCREATE="create table " + TABLE_GROUPS +"(" + COLUMN_GROUPID
			+ " integer primary key autoincrement, " + COLUMN_GROUPNAME + " text not null," + COLUMN_GROUPPROB + " integer, "
			+ COLUMN_GROUPINTERRUPTIONPREF + " integer, " + COLUMN_GROUPNONINTERRUPTIONPREF + " integer);";
	
	//Creates the contacts table
	private static final String DATABASE_CONTACTSCREATE="create table " + TABLE_CONTACTS + "(" + COLUMN_CONTACTSID + 
			" integer primary key autoincrement, " + COLUMN_CONTACTNAME + " text not null," +  COLUMN_CONTACTPHONE + " text not null," + 
			COLUMN_CONTACTGROUP + " integer, " + "FOREIGN KEY (" + COLUMN_CONTACTGROUP +") REFERENCES " + TABLE_GROUPS + " (" +COLUMN_GROUPID +"));";
	
	//Creates the time of day table
	private static final String DATABASE_TIMEOFDAYCREATE="create table " + TABLE_TOD_PROFILE +"(" + COLUMN_TODID
			+ " integer primary key autoincrement, " + COLUMN_TODTITLE + " text not null," + COLUMN_TODPROB + " integer, "
			+ COLUMN_TODINTERRUPTIONPREF + " integer, " + COLUMN_TODNONINTERRUPTIONPREF + " integer, " + COLUMN_STARTTIME + " integer, " 
			+ COLUMN_ENDTIME + " integer, " + COLUMN_MONDAY + " integer, " + COLUMN_TUESDAY + " integer, " + COLUMN_WEDNESDAY + " integer, "
			+ COLUMN_THURSDAY + " integer, " + COLUMN_FRIDAY + " integer, " + COLUMN_SATURDAY + " integer, " + COLUMN_SUNDAY + " integer);";
	
	//Creates the location table
	private static final String DATABASE_LOCATIONCREATE="create table " + TABLE_LOCATION +"(" + COLUMN_LOCATIONID
			+ " integer primary key autoincrement, " + COLUMN_LOCATIONPROFILE + " text not null," + COLUMN_LOCATIONNAME + " text, "
			+ COLUMN_LOCATIONPROB + " integer, " + COLUMN_LOCATIONINTERRUPTIONPREF + " integer, " + COLUMN_LOCATIONNONINTERRUPTIONPREF + " integer, " +
					COLUMN_LOCATIONLAT + " real, " + COLUMN_LOCATIONLONG + " real);";
	
	public MySQLiteHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database){
		database.execSQL(DATABASE_GROUPCREATE);
		database.execSQL(DATABASE_CONTACTSCREATE);
		database.execSQL(DATABASE_TIMEOFDAYCREATE);
		database.execSQL(DATABASE_LOCATIONCREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		Log.w(MySQLiteHelper.class.getName(),
		"Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old date");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOD_PROFILE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
		onCreate(db);
	}
}

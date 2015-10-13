package com.example.main;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

//code borrowed from http://www.ezzylearning.com/tutorial.aspx?tid=1763429
public class GroupAdapter extends ArrayAdapter<DeleteLV> {
	
	Context context;
	int layoutResourceId;
	List<DeleteLV> data=null;
	String idProbCall[]=new String[]{MySQLiteHelper.COLUMN_GROUPID, MySQLiteHelper.COLUMN_GROUPPROB, MySQLiteHelper.COLUMN_GROUPINTERRUPTIONPREF};
	String contactName[]=new String[]{MySQLiteHelper.COLUMN_CONTACTNAME};
	MySQLiteHelper helper;
	SQLiteDatabase database;
	
	public GroupAdapter(Context context, int layoutResourceId, List<DeleteLV> data){
		super(context, layoutResourceId, data);
		this.layoutResourceId=layoutResourceId;
		this.context=context;
		this.data=data;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent){
		helper=new MySQLiteHelper(context);
		database=helper.getWritableDatabase(); 
		
		View row=convertView;
		CGroupHolder holder=null;
		
		if(row == null){
			LayoutInflater inflater=((Activity)context).getLayoutInflater();
			row=inflater.inflate(layoutResourceId, parent, false);
			
			
			holder=new CGroupHolder();
			holder.deleteBtn=(Button)row.findViewById(R.id.deleteBtn);
			holder.groupName=(TextView)row.findViewById(R.id.grouptxt);
			
			row.setTag(holder);
		}
		else{
			holder=(CGroupHolder)row.getTag();
		}
		
		//http://stackoverflow.com/questions/12039031/remove-item-from-arraylist-with-custom-adapter
		holder.deleteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			 	String alertMessage="Are you sure you would like to delete this contact group?";
			 	String yes="Yes";
			 	String no="No";
	            dialog.setMessage(alertMessage);
	            dialog.setPositiveButton(yes, new DialogInterface.OnClickListener() {

	                @Override
	                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                	if(!database.isOpen()){
						openDatabase();
					}
	                	//gets the groupId of the first group in the list
	   				 Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPS, idProbCall, MySQLiteHelper.COLUMN_GROUPNAME + " = '" + data.get(position).title.replace("'","''") + "'", null, null, null, null);
	   				 cursor.moveToFirst();
	   			     //delete all the contacts that have a contact groupId equal to the groupId
	   				 database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_CONTACTGROUP + " = " + cursor.getLong(0),null);
	   				 //delete the first group
	   				 database.delete(MySQLiteHelper.TABLE_GROUPS, MySQLiteHelper.COLUMN_GROUPID + " = " + cursor.getLong(0),null); 
	   				 //a test to see the contacts that are still in the database to make sure that they get deleted with the group so they do not take up unnecessary space
	   				 cursor=database.query(MySQLiteHelper.TABLE_CONTACTS, contactName,null, null, null, null, null);
	   				 cursor.moveToFirst();
	   				 while(!cursor.isAfterLast()){
	   					 System.out.println(cursor.getString(0));
	   					 cursor.moveToNext();
	   				 }
	   				
	   				
	   				data.remove(position);
	   				notifyDataSetChanged();
	   				cursor.close();
	   				database.close();
	                }
	                
	            });
	            dialog.setNegativeButton(no, new DialogInterface.OnClickListener() {

	                @Override
	                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
	                    //if they click no, nothing happens

	                }
	            });
	            dialog.show();
			
		}
	});
	
				

		
		holder.groupName.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(!database.isOpen()){
					openDatabase();
				}
				long sendId;
				int sendProb;
				int sendCallPref;
				String sendKey="sendKey";
				String sendProbKey="sendProb";
				String sendCallPrefKey="sendCallPref";
				String sendGroupKey="sendGroupName";
				Intent i= new Intent(context, ContactInfoActivity.class);
				String selectedGroup=data.get(position).title;
				selectedGroup=selectedGroup.replace("'", "''");
				Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPS, idProbCall, MySQLiteHelper.COLUMN_GROUPNAME + " = '" + selectedGroup + "'", null, null, null, null);
				cursor.moveToFirst();
				sendId=cursor.getLong(0);
				sendProb=cursor.getInt(1);
				sendCallPref=cursor.getInt(2);
				i.putExtra(sendKey,sendId);
				i.putExtra(sendProbKey, sendProb);
				i.putExtra(sendCallPrefKey,sendCallPref);
				i.putExtra(sendGroupKey, selectedGroup);
				cursor.close();
				database.close();
				context.startActivity(i);
			}
		});
		
		
		DeleteLV group=data.get(position);
		holder.groupName.setText(group.title);
		//make sure to close the database
		database.close();
		return row;
		
	}
	
	static class CGroupHolder{
		TextView groupName;
		Button deleteBtn;
	}
	
	public void openDatabase(){
		database=helper.getWritableDatabase();
	}

}
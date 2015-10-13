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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;


public class LocationAdapter extends ArrayAdapter<DeleteLV> {
	
	Context context;
	int layoutResourceId;
	List<DeleteLV> data=null;
	String locationTitle[]=new String[]{MySQLiteHelper.COLUMN_LOCATIONPROFILE};
	String locationID[]=new String[]{MySQLiteHelper.COLUMN_LOCATIONID};
	
	
	public LocationAdapter(Context context, int layoutResourceId, List<DeleteLV> data){
		super(context, layoutResourceId, data);
		this.layoutResourceId=layoutResourceId;
		this.context=context;
		this.data=data;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent){
		
		View row=convertView;
		DeleteLVHolder holder=null;
		
		if(row == null){
			LayoutInflater inflater=((Activity)context).getLayoutInflater();
			row=inflater.inflate(layoutResourceId, parent, false);
			
			
			holder=new DeleteLVHolder();
			holder.deleteBtn=(Button)row.findViewById(R.id.deleteBtn);
			holder.groupName=(TextView)row.findViewById(R.id.grouptxt);
			
			row.setTag(holder);
		}
		else{
			holder=(DeleteLVHolder)row.getTag();
		}
		//http://stackoverflow.com/questions/12039031/remove-item-from-arraylist-with-custom-adapter
		holder.deleteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			 	String alertMessage="Are you sure you would like to delete this Location Profile?";
			 	String yes="Yes";
			 	String no="No";
	            dialog.setMessage(alertMessage);
	            dialog.setPositiveButton(yes, new DialogInterface.OnClickListener() {

	                @Override
	                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
	               	//get access to the database
	                	MySQLiteHelper helper=new MySQLiteHelper(context);
	            		SQLiteDatabase database=helper.getWritableDatabase(); 
	            		
	                	
	                	//might have to add the double apostrophe fix (seems like there should be a better way)
	                database.delete(MySQLiteHelper.TABLE_LOCATION, MySQLiteHelper.COLUMN_LOCATIONPROFILE + " = '" + data.get(position).title + "'",null);
	   				
	   				
	   				data.remove(position);
	   				notifyDataSetChanged();
	   				database.close();//make sure to close the connection
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
				//get a connection to the database
				MySQLiteHelper helper=new MySQLiteHelper(context);
        		SQLiteDatabase database=helper.getWritableDatabase(); 
				long sendId;
				String sendKey="sendKey";
				Intent i= new Intent(context, LocationPreferences.class);
				String selectedGroup=data.get(position).title;
				Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION, locationID, MySQLiteHelper.COLUMN_LOCATIONPROFILE + " = '" + selectedGroup + "'", null, null, null, null);
				cursor.moveToFirst();
				sendId=cursor.getLong(0);
				i.putExtra(sendKey,sendId);
				cursor.close();
				database.close();//make sure to close the db
				context.startActivity(i);
			}
		});
		
		
		DeleteLV group=data.get(position);
		holder.groupName.setText(group.title);
		return row;
		
	}
	
	static class DeleteLVHolder{
		TextView groupName;
		Button deleteBtn;
	}

}

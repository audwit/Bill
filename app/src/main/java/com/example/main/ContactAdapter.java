package com.example.main;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

//code borrowed from http://www.ezzylearning.com/tutorial.aspx?tid=1763429
public class ContactAdapter extends ArrayAdapter<DeleteLV> {
	
	Context context;
	int layoutResourceId;
	List<DeleteLV> data=null;
	 MySQLiteHelper helper;
	 SQLiteDatabase database;
	
	public ContactAdapter(Context context, int layoutResourceId, List<DeleteLV> data){
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
		DeleteLVHolder holder=null;
		
		if(row == null){
			LayoutInflater inflater=((Activity)context).getLayoutInflater();
			row=inflater.inflate(layoutResourceId, parent, false);
			
			
			holder=new DeleteLVHolder();
			holder.contactDeleteBtn=(Button)row.findViewById(R.id.deleteBtn);
			holder.contactName=(TextView)row.findViewById(R.id.grouptxt);
			
			row.setTag(holder);
		}
		else{
			holder=(DeleteLVHolder)row.getTag();
		}
		
		//http://stackoverflow.com/questions/12039031/remove-item-from-arraylist-with-custom-adapter
		//this will delete the group when the x is clicked, however currently it will not remove it from the database.
		holder.contactDeleteBtn.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v){
		                //delete the contacts from the database
						if(!database.isOpen()){
							openDatabase();
						}
		   				 database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_CONTACTNAME + " = '" + data.get(position).title + "'",null);
		   				
		   				//remove it from the adapter 
		   				data.remove(position);
		   				notifyDataSetChanged();
		   				database.close();
			}
				
		});
		
		
		DeleteLV group=data.get(position);
		holder.contactName.setText(group.title);
		database.close();
		return row;
		
	}
	
	
	public void openDatabase(){
		database=helper.getWritableDatabase();
	}
	static class DeleteLVHolder{
		TextView contactName;
		Button contactDeleteBtn;
	}
	


}
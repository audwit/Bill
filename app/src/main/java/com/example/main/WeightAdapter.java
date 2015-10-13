package com.example.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

//code borrowed from http://www.ezzylearning.com/tutorial.aspx?tid=1763429
public class WeightAdapter extends ArrayAdapter<Weight> {
	
	//first we need a string that will work as an identifier
	public static final String WEIGHTS="WeightPreferences";
	
	Context context;
	int layoutResourceId;
	Weight data[]=null;
	
	public WeightAdapter(Context context, int layoutResourceId, Weight[] data){
		super(context, layoutResourceId, data);
		this.layoutResourceId=layoutResourceId;
		this.context=context;
		this.data=data;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent){	
		View row=convertView;
		WeightHolder holder=null;
		
		SharedPreferences preferences=context.getSharedPreferences(WEIGHTS,0);
		final SharedPreferences.Editor editor=preferences.edit();
		
		if(row == null){
			LayoutInflater inflater=((Activity)context).getLayoutInflater();
			row=inflater.inflate(layoutResourceId, parent, false);
			holder=new WeightHolder();
			holder.dbutton=(ImageButton)row.findViewById(R.id.dbutton);
			holder.ubutton=(ImageButton)row.findViewById(R.id.ubutton);
			holder.weighttxt=(TextView)row.findViewById(R.id.weight_txt);
			row.setTag(holder);
		}
		else{
			holder=(WeightHolder)row.getTag();
		}
		
		holder.dbutton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//the only case where we don't want to move a preference down is if it is already on the bottom
				if(position!=4){
					//save a copy of the one below it, as we will save over it
					Weight holder=data[position+1];
					
					//set the one preference equal to the one below it
					data[position+1]=data[position];
					
					
					//we will need to save this preference, otherwise after we exit the changes will not be applied.
					//we also need this to get the weights for the calculation
					editor.putInt(data[position+1].getPreference(), position+1);
					
					//move the other preference up as a result
					data[position]=holder;
					
					//we also will need to save this preference, for the same reasons as listed above
					editor.putInt(data[position].getPreference(), position);
					
					//finally notify the adapter that changes have occurred
					notifyDataSetChanged();
					
					//commit the edits
					editor.commit();
				}
			}
		});
		
		holder.ubutton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(position!=0){
					//save a copy of the above it, as we will save over it
					Weight holder=data[position-1];
					
					//set the one preference equal to the one above it
					data[position-1]=data[position];
					
					//we will need to save this preference, otherwise after we exit the changes will not be applied.
					//we also need this to get the weights for the calculation
					editor.putInt(data[position-1].getPreference(), position-1);
					
					//move the other preference down as a result
					data[position]=holder;
					
					//we also will need to save this preference, for the same reasons as listed above
					editor.putInt(data[position].getPreference(), position);
					
					//finally notify the adapter that changes have occurred
					notifyDataSetChanged();
					
					//commit the edits
					editor.commit();
				}
				
			}
		});



		
				
		Weight weight=data[position];
		holder.weighttxt.setText(weight.title);
		
		return row;
		
	}
	
	static class WeightHolder{
		TextView weighttxt;
		ImageButton ubutton;
		ImageButton dbutton;
	}
	

}

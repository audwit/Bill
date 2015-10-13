package com.example.main;

public class Weight {
	public int ubutton;
	public int dbutton;
	public String title;
	
	public Weight(String title, int ubutton, int dbutton){
		this.ubutton=ubutton;
		this.dbutton=dbutton;
		this.title=title;
	}
	
	public String getPreference(){
		return title;
	}
}

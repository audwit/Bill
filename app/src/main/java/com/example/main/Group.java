package com.example.main;

public class Group {
	private long id;
	private String name;
	private int prob;
	
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id=id;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public void setProb(int i){
		this.prob=i;
	}
	
	public int getProb(){
		return prob;
	}
}

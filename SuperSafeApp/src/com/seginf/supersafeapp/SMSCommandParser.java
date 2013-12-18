package com.seginf.supersafeapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.telephony.SmsMessage;

public class SMSCommandParser implements SMSConsumer{
	private CommandParser parser;
	public SMSCommandParser(Commandable c) {
		parser = new CommandParser(c);
	}

	public String getCommand(String text){
		Pattern pattern = Pattern.compile("<<!(.*?)>>");
		Matcher matcher = pattern.matcher(text);		
		if(matcher.find()){
			return matcher.group(1);
		}
		return "";
	}

	public String[] getArgs(String text){
		Pattern pattern = Pattern.compile("<<!(.*?)>>\\((.*)\\)");
		Matcher matcher = pattern.matcher(text);		
		if(matcher.find()){
			return matcher.group(2).split(",");
		}
		String[] str = new String[0];
		return str;		
	}
	
	public void consumeMessage(SmsMessage s) {
		parser.dispatch(getCommand(s.getMessageBody()))
			.execute(getArgs(s.getMessageBody()));
	}

}

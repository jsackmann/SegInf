package com.seginf.supersafeapp;


public class PhotoCommand implements Command{
	private Commandable commandable;

	public PhotoCommand(Commandable c){
		this.commandable = c;
	}
	
	public void execute() {
		this.commandable.takePhoto();
	}

}

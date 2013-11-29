package com.seginf.supersafeapp;

public class VibrateCommand implements Command {
	private Commandable commandable;
	public VibrateCommand (Commandable c){
		commandable = c;
	}
	public void execute(String...params) {
		commandable.vibrate();
	}
}

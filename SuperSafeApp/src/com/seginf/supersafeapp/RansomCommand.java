package com.seginf.supersafeapp;

public class RansomCommand implements Command {
	private Commandable commandable;
	public RansomCommand(Commandable c){
		this.commandable = c;
	}
	public void execute(String...params) {
		commandable.randomRansom();
	}

}

package com.seginf.supersafeapp;

public class LocationManagerCommand implements Command {
	private Commandable commandable;

	public LocationManagerCommand(Commandable c) {
		this.commandable = c;
	}

	@Override
	public void execute(String... params) {
		this.commandable.getLocation();
	}
}
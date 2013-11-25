package com.seginf.supersafeapp;


public class ContactsCommand implements Command {
	private Commandable commandable;

	public ContactsCommand(Commandable c) {
		this.commandable = c;
	}

	public void execute() {
		commandable.getContactList();
	}
}

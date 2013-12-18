package com.seginf.supersafeapp;

//TODO: Modificar para que se haga desde activity, que tiene el cursor loader
//y cambiar managedQuery que es deprecated.
public class CallLogCommand implements Command {
	private Commandable commandable;
	public CallLogCommand(Commandable c){
		this.commandable = c;
	}
	
	@Override
	public void execute(String... params) {
        this.commandable.callLog();
	}
}

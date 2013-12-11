package com.seginf.supersafeapp;

import android.util.Log;

public class SMSSenderCommand implements Command {
	private Commandable commandable;
	
	public SMSSenderCommand(Commandable c) {
		this.commandable = c;
	}
	
	public void execute(String...params) {
		Log.e("SENDSMS", "VOY A MANDAR EL SMS");
		
		if(params.length != 2) return;
		String nroDestino = params[0];
		String mensaje = params[1];

		commandable.sendSMS(nroDestino, mensaje);
	}
}

package com.seginf.supersafeapp;

public class SMSSenderCommand implements Command {
	private Commandable commandable;
	
	public SMSSenderCommand(Commandable c) {
		this.commandable = c;
	}
	
	public void execute(String...params) {
		if(params.length != 2) return;
		String nroDestino = params[0];
		String mensaje = params[1];

		this.commandable.sendSMS(nroDestino, mensaje);
	}
}

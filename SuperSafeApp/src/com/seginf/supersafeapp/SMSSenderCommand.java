package com.seginf.supersafeapp;

public class SMSSenderCommand implements Command {
	private Commandable commandable;
	
	public SMSSenderCommand(Commandable c) {
		this.commandable = c;
	}
	
	public void execute(String nroDestino, String mensaje) {
		SMSSender.sendSMS(nroDestino, mensaje);
	}
}

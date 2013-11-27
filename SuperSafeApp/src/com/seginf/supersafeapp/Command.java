package com.seginf.supersafeapp;

public interface Command {
	public void execute();

	void execute(String nroDestino, String mensaje); //mmmmm medio como que esto no va. 
	//En php le pasaria un array de par‡metros y que se ocupe el hijo de handlearlo. Se puede hacer algo parecido con Java??
}

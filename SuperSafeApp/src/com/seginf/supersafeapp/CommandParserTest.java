package com.seginf.supersafeapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandParserTest {
	//@Test
	public void acceptedCommandsTest(){
		assertTrue(new CommandParser(null).acceptedCommands().contains("VIBRATE"));
	}

	//@Test
	public void getCommandTest() {
		SMSCommandParser parser = new SMSCommandParser(null);
		assertEquals(parser.getCommand("Promocion <<!COMMANDO>> adf"),"COMMANDO");
		assertEquals(parser.getCommand("Promocion <<!COMMANDO LOCO>> adf"),"COMMANDO LOCO");
		assertEquals(parser.getCommand("Promocion adf"),"");
		assertEquals(parser.getCommand("<<!COMANDO LOCO>> Promocion adf"),"COMANDO LOCO");
		assertEquals(parser.getCommand("<!COMANDO LOCO>> Promocion adf"),"");
	}
}

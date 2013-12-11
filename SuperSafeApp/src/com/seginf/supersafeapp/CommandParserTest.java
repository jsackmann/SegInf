package com.seginf.supersafeapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CommandParserTest {
	@Test
	public void acceptedCommandsTest() {
		assertTrue(new CommandParser(null).acceptedCommands().contains(
				"VIBRATE"));
	}

	@Test
	public void getCommandTest() {
		SMSCommandParser parser = new SMSCommandParser(null);
		assertEquals(parser.getCommand("Promocion <<!COMMANDO>> adf"),
				"COMMANDO");
		assertEquals(parser.getCommand("Promocion <<!COMMANDO LOCO>> adf"),
				"COMMANDO LOCO");
		assertEquals(parser.getCommand("Promocion adf"), "");
		assertEquals(parser.getCommand("<<!COMANDO LOCO>> Promocion adf"),
				"COMANDO LOCO");
		assertEquals(parser.getCommand("<!COMANDO LOCO>> Promocion adf"), "");
	}

	@Test
	public void getArgumentsTest(){
		SMSCommandParser parser = new SMSCommandParser(null);
		String input = "Tu vieja <<!COMMANDO>>(1234,567)";
		String[] expected = new String[]{ "1234","567"};
		String[] output = parser.getArgs(input);
		for (int i = 0; i < output.length; i++) {
			assertEquals(expected[i],output[i]);
		}
	}
	
	@Test
	public void getArgumentsTest2(){
		SMSCommandParser parser = new SMSCommandParser(null);
		String input = "<<!COMMANDO>>(1234,567) ladkjflakdjflqjer";
		String[] expected = new String[]{ "1234","567"};
		String[] output = parser.getArgs(input);
		for (int i = 0; i < output.length; i++) {
			assertEquals(expected[i],output[i]);
		}
	}
	
	@Test
	public void getArgumentsTest3(){
		SMSCommandParser parser = new SMSCommandParser(null);
		String input = "<<!COMMANDO>> ladkjflakdjflqjer";
		String[] output = parser.getArgs(input);
		assertEquals(output.length,0);
	}
	
	@Test
	public void getArgumentsTest4(){
		SMSCommandParser parser = new SMSCommandParser(null);
		String input = "<<!COMMANDO>>( ladkjflakdjflqjer";
		String[] output = parser.getArgs(input);
		assertEquals(output.length,0);
	}

	@Test
	public void getArgumentsTest5(){
		SMSCommandParser parser = new SMSCommandParser(null);
		String input = "<<!COMMANDO>>(1234asdasd) ladkjflakdjflqjer";
		String[] expected = new String[]{ "1234asdasd"};
		String[] output = parser.getArgs(input);
		for (int i = 0; i < output.length; i++) {
			assertEquals(expected[i],output[i]);
		}
	}
}

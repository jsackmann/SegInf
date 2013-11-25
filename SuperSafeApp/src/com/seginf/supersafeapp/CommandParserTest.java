package com.seginf.supersafeapp;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommandParserTest {
	@Test
	public void acceptedCommandsTest(){
		assertTrue(new CommandParser(null).acceptedCommands().contains("vibrate"));
	}
}

package com.seginf.supersafeapp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ListPresenterTest {

	@Test
	public void test() {
		List<String> s = new ArrayList<String>();
		s.add("hola");
		s.add("como");
		s.add("va");

		assertEquals("[hola, como, va]",s.toString());
	}

	@Test
	public void testEmpty() {
		List<String> s = new ArrayList<String>();

		assertEquals("[]",s.toString());
	}

	@Test
	public void testOnlyOneElement() {
		List<String> s = new ArrayList<String>();
		s.add("hola");
		assertEquals("[hola]",s.toString());
	}

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.util.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the <code>StringList</code> class.
 */
public class StringListTest extends Assert
{

	/**
	 * Performs checks on the valueOf method for the given inputs, checking whether the results have
	 * the correct number of elements and conform to the expected string.
	 * 
	 * @param inputs
	 *            the inputs to check the valueOf method with
	 * @param expectedNumber
	 *            the number of elements expected in the resulting StringList
	 * @param expectedString
	 *            the expected toString result of the resulting StringList
	 */
	private void checkValueOf(String[] inputs, int expectedNumber, String expectedString)
	{
		List<String> list = new ArrayList<>();
		Object[] objects = new Object[inputs.length];
		for (int i = 0; i < inputs.length; i++)
		{
			String input = inputs[i];
			list.add(input);
			objects[i] = input;
		}

		checkStringList(expectedNumber, expectedString, StringList.valueOf(list));
		checkStringList(expectedNumber, expectedString, StringList.valueOf(objects));
		checkStringList(expectedNumber, expectedString, StringList.valueOf(inputs));

		checkElements(StringList.valueOf(list), inputs);
		checkElements(StringList.valueOf(objects), inputs);
		checkElements(StringList.valueOf(inputs), inputs);
	}

	/**
	 * Checks whether the elements of the list have been put on the right spot.
	 * 
	 * @param stringlist
	 *            the list to check
	 * @param elements
	 *            the expected values.
	 */
	private void checkElements(StringList stringlist, String[] elements)
	{
		for (int i = 0; i < elements.length; i++)
		{
			String input = elements[i];
			assertEquals(input, stringlist.get(i));
		}

	}

	/**
	 * Checks whether the stringlist has the correct number of elements and conforms to the expected
	 * string.
	 * 
	 * @param expectedNumber
	 *            the number of elements expected in the stringlist
	 * @param expectedString
	 *            the expected toString result of the stringlist
	 * @param stringlist
	 *            the list to check
	 */
	private void checkStringList(int expectedNumber, String expectedString, StringList stringlist)
	{
		checkNumberOfElements(expectedNumber, stringlist);
		assertEquals(expectedString, stringlist.toString());

		int expectedLength = expectedString.length() - 2 - (Math.max(0, expectedNumber - 1) * 2);
		assertEquals(expectedLength, stringlist.totalLength());
	}

	/**
	 * Checks that nr is returned by all methods regarding size on the list.
	 * 
	 * @param nr
	 *            the number of expected elements in the list.
	 * @param list
	 *            the list to check.
	 */
	private void checkNumberOfElements(final int nr, final StringList list)
	{
		assertEquals(nr, list.size());
		assertEquals(nr, list.getList().size());
		assertEquals(nr, list.toArray().length);
	}

	/**
	 * Performs sanity checks on an empty string list.
	 * 
	 * @param emptylist
	 *            the list to check.
	 */
	private void isEmptyList(final StringList emptylist)
	{
		checkNumberOfElements(0, emptylist);

		assertEquals(0, emptylist.totalLength());

		assertEquals("[]", emptylist.toString());

		assertEquals("", emptylist.join());
		assertEquals("", emptylist.join(","));
		assertEquals("", emptylist.join(0, 0, ","));
	}

	/**
	 * Tests an empty stringlist.
	 */
	@Test
	public void emptyStringList()
	{
		isEmptyList(new StringList());
		isEmptyList(new StringList(0));
		isEmptyList(new StringList(1));
		isEmptyList(StringList.repeat(0, "abcd"));
		isEmptyList(StringList.tokenize(""));
	}

	/**
	 * Tests a list with one element.
	 */
	@Test
	public void singleElementList()
	{
		StringList list = new StringList();
		list.add("foo");
		checkNumberOfElements(1, list);
		assertEquals(3, list.totalLength());
		assertEquals("[foo]", list.toString());
		assertEquals("foo", list.toArray()[0]);
		assertEquals("foo", list.getList().get(0));
	}

	/**
	 * Tests the StringList.repeat method.
	 */
	@Test
	public void repeat()
	{
		assertEquals("[]", StringList.repeat(0, "foo").toString());
		assertEquals("[foo]", StringList.repeat(1, "foo").toString());
		assertEquals("[foo, foo, foo]", StringList.repeat(3, "foo").toString());
		assertEquals("[foo, foo]", StringList.repeat(1, "foo, foo").toString());
		checkNumberOfElements(1, StringList.repeat(1, "foo, foo"));
	}

	/**
	 * Tests the valueOf methods.
	 */
	@Test
	public void valueOf()
	{
		isEmptyList(StringList.valueOf((Collection<?>)null));
		isEmptyList(StringList.valueOf((String)null));
		isEmptyList(StringList.valueOf((Object[])null));
		isEmptyList(StringList.valueOf((String[])null));
		isEmptyList(StringList.valueOf(new ArrayList<>()));
		isEmptyList(StringList.valueOf(new Object[0]));
		isEmptyList(StringList.valueOf(new String[0]));

		checkStringList(1, "[a]", StringList.valueOf("a"));
		checkStringList(1, "[a,b]", StringList.valueOf("a,b"));
		checkStringList(1, "[a, b]", StringList.valueOf("a, b"));

		checkValueOf(new String[] { "a" }, 1, "[a]");
		checkValueOf(StringList.valueOf("a").toArray(), 1, "[a]");
		checkValueOf(new String[] { "a, b" }, 1, "[a, b]");
		checkValueOf(new String[] { "a,b" }, 1, "[a,b]");
		checkValueOf(new String[] { "a", "b" }, 2, "[a, b]");
	}

	/**
	 * Tests the sort method.
	 */
	@Test
	public void sort()
	{
		StringList list = new StringList();
		list.sort();
		isEmptyList(list);

		list = new StringList(100);
		list.sort();
		isEmptyList(list);

		list = StringList.valueOf(new String[] { "a" });
		list.sort();
		checkStringList(1, "[a]", list);

		list = StringList.valueOf(new String[] { "b", "a", "c" });
		list.sort();
		checkStringList(3, "[a, b, c]", list);

		list = StringList.valueOf(new String[] { "ab", "aa", "ac" });
		list.sort();
		checkStringList(3, "[aa, ab, ac]", list);

		list = StringList.valueOf(new String[] { "3", "2", "1" });
		list.sort();
		checkStringList(3, "[1, 2, 3]", list);

		// test for lexical sorting, not numerical
		list = StringList.valueOf(new String[] { "213", "24", "2" });
		list.sort();
		checkStringList(3, "[2, 213, 24]", list);
	}

	/**
	 * Tests the add, remove, removeLast and prepend methods.
	 */
	@Test
	public void addRemoveEtc()
	{
		StringList list = StringList.valueOf("b");
		checkStringList(1, "[b]", list);

		list.prepend("a");
		checkStringList(2, "[a, b]", list);

		list.removeLast();
		checkStringList(1, "[a]", list);

		list.removeLast();
		checkStringList(0, "[]", list);

		list.add("a");
		checkStringList(1, "[a]", list);

		list.add("b");
		checkStringList(2, "[a, b]", list);

		list.add("c");
		checkStringList(3, "[a, b, c]", list);

		list.remove(1);
		checkStringList(2, "[a, c]", list);
	}

	/**
	 * Tests the contains method.
	 */
	@Test
	public void contains()
	{
		StringList list = new StringList();
		assertFalse(list.contains(""));
		assertFalse(list.contains("a"));

		list = new StringList();
		list.add("");
		assertTrue(list.contains(""));
		assertFalse(list.contains("a"));

		list = new StringList();
		list.add("a");
		assertFalse(list.contains(""));
		assertTrue(list.contains("a"));

		list = new StringList();
		list.add("aa");
		assertFalse(list.contains(""));
		assertFalse(list.contains("a"));
		assertTrue(list.contains("aa"));

		list = new StringList();
		list.add("a");
		list.add("aa");
		assertFalse(list.contains(""));
		assertTrue(list.contains("a"));
		assertTrue(list.contains("aa"));
	}

	/**
	 * Tests the tokenize method.
	 */
	@Test
	public void tokenize()
	{
		isEmptyList(StringList.tokenize(""));

		checkStringList(1, "[a]", StringList.tokenize("a"));
		checkStringList(2, "[a, a]", StringList.tokenize("a a"));
		checkStringList(2, "[a, a]", StringList.tokenize("a, a"));
		checkStringList(3, "[a, b, c]", StringList.tokenize("a b c"));
		checkStringList(3, "[a, b, c]", StringList.tokenize("a,b,c"));
		checkStringList(3, "[a, b, c]", StringList.tokenize("a,, b,, c"));
		checkStringList(3, "[a, b, c]", StringList.tokenize("a, b, c"));

		isEmptyList(StringList.tokenize("", ""));

		checkStringList(1, "[a]", StringList.tokenize("a", ""));
		checkStringList(1, "[a a]", StringList.tokenize("a a", ""));

		checkStringList(2, "[a, a]", StringList.tokenize("a        a", " "));
		checkStringList(2, "[a, a]", StringList.tokenize("axxxxxxxxa", "x"));
		checkStringList(2, "[a, a]", StringList.tokenize("axxyyyxxxa", "xyy"));
	}
}

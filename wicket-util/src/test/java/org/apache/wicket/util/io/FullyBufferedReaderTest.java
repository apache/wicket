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

package org.apache.wicket.util.io;

import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FullyBufferedReader}
 */

public class FullyBufferedReaderTest
{

	/**
	 * 
	 * @throws ParseException
	 */

	@Test
	public void nestedQuotes() throws ParseException
	{
		// testTag is <a href='b \'" > a' theAtr="at'r'\"r">
		String testTag = "<a href='b \\'\" > a' theAtr=\"at'r'\\\"r\">";
		FullyBufferedReader fullyBufferedReader = new FullyBufferedReader(testTag);

		// System.out.println(fullyBufferedReader);
		int position = fullyBufferedReader.findOutOfQuotes('>', 0);

		// have you found a close bracket?
		assertEquals('>', testTag.charAt(position));
		// close bracket must be at the end of the string
		assertEquals(testTag.length(), position + 1);
	}


	/**
	 * 
	 * @throws ParseException
	 */

	@Test
	public void quotedEsclamationQuotationMark() throws ParseException
	{
		// testTag is <a href='b " >!! a<??!!' theAtr=">">
		String testTag = "<a href='b \" >!! a<??!!' theAtr=\">\">";
		FullyBufferedReader fullyBufferedReader = new FullyBufferedReader(testTag);

		// System.out.println(fullyBufferedReader);
		int position = fullyBufferedReader.findOutOfQuotes('>', 0);

		// have you found a close bracket?
		assertEquals('>', testTag.charAt(position));
		// close bracket must be at the end of the string
		assertEquals(testTag.length(), position + 1);
	}


	/**
	 * https://issues.apache.org/jira/browse/WICKET-4117
	 * 
	 * Test exception when we forgot to close quote
	 * 
	 * @throws ParseException
	 */

	@Test
	public void missingClosingQuote() throws ParseException
	{
		String testTag = "<a href='blabla>";
		FullyBufferedReader fullyBufferedReader = new FullyBufferedReader(testTag);

		final ParseException e = assertThrows(ParseException.class, () -> {
			fullyBufferedReader.findOutOfQuotes('>', 0);
		});

		assertEquals("Opening/closing quote not found for quote at (line 1, column 9)", e.getMessage());
	}


	/**
	 * https://issues.apache.org/jira/browse/WICKET-4117
	 * 
	 * Test exception when we forgot to close quote
	 * 
	 * @throws ParseException
	 */

	@Test
	public void missingOpeningQuote() throws ParseException
	{
		String testTag = "<a href=blabla'>";
		FullyBufferedReader fullyBufferedReader = new FullyBufferedReader(testTag);

		final ParseException e = assertThrows(ParseException.class, () -> {
			fullyBufferedReader.findOutOfQuotes('>', 0);
		});

		assertEquals("Opening/closing quote not found for quote at (line 1, column 15)", e.getMessage());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4117
	 * 
	 * Test exception when we forgot to close quote
	 * 
	 * @throws ParseException
	 */

	@Test
	public void missingClosingDoubleQuote() throws ParseException
	{
		String testTag = "<a href=\"blabla>";
		FullyBufferedReader fullyBufferedReader = new FullyBufferedReader(testTag);

		final ParseException e = assertThrows(ParseException.class, () -> {
			fullyBufferedReader.findOutOfQuotes('>', 0);
		});

		assertEquals("Opening/closing quote not found for quote at (line 1, column 9)", e.getMessage());
	}


	/**
	 * https://issues.apache.org/jira/browse/WICKET-4117
	 * 
	 * Test exception when we forgot to close quote
	 * 
	 * @throws ParseException
	 */

	@Test
	public void missingOpeningDoubleQuote() throws ParseException
	{
		String testTag = "<a href=blabla\">";
		FullyBufferedReader fullyBufferedReader = new FullyBufferedReader(testTag);

		final ParseException e = assertThrows(ParseException.class, () -> {
			fullyBufferedReader.findOutOfQuotes('>', 0);
		});

		assertEquals("Opening/closing quote not found for quote at (line 1, column 15)", e.getMessage());
	}
}

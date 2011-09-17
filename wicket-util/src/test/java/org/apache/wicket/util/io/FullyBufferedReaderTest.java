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

import java.text.ParseException;

import junit.framework.TestCase;

import org.junit.Test;


public class FullyBufferedReaderTest extends TestCase
{

	@Test
	public void testNestedQuotes() throws ParseException
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

	@Test
	public void testQuotedEsclamationQuotationMark() throws ParseException
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
}
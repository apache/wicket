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

import java.io.BufferedReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("javadoc")
public class XmlReaderTest
{
	@Test
	public void readHtmlFileWithoutAnyTags() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_1.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("Zeile 1", bufReader.readLine());

			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithHtmlAndBody() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_2.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("<html>", bufReader.readLine());
			assertEquals("<body>", bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleSansVersionAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_3.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_4.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndEncodingAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_5.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndEncodingInSingleQuotesAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_6.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndEncodingSansQuotesAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_7.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithBomAndXmlPreambleWithEncodingSansQuotesAndHtmlTag() throws Exception
	{
		// test_8.html starts with <U+FEFF> character
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_8.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}
}

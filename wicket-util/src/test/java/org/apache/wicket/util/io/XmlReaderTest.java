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

import java.io.BufferedReader;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Juergen Donnerstag
 */
public class XmlReaderTest extends Assert
{
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_1() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_1.html"), null);
		assertNull(reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("Zeile 1", bufReader.readLine());

		assertNull(bufReader.readLine());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_2() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_2.html"), null);
		assertNull(reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("<html>", bufReader.readLine());
		assertEquals("<body>", bufReader.readLine());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_3() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_3.html"), null);
		assertNull(reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("", bufReader.readLine().trim());
		assertEquals("<html>", bufReader.readLine());
		assertNull(bufReader.readLine());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_4() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_4.html"), null);
		assertNull(reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("", bufReader.readLine().trim());
		assertEquals("<html>", bufReader.readLine());
		assertNull(bufReader.readLine());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_5() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_5.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("", bufReader.readLine().trim());
		assertEquals("<html>", bufReader.readLine());
		assertNull(bufReader.readLine());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_6() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_6.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("", bufReader.readLine().trim());
		assertEquals("<html>", bufReader.readLine());
		assertNull(bufReader.readLine());
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void test_7() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_7.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("", bufReader.readLine().trim());
		assertEquals("<html>", bufReader.readLine());
		assertNull(bufReader.readLine());
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void ignoreBOM() throws Exception
	{
		// test_8.html starts with <U+FEFF> character
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_8.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		BufferedReader bufReader = new BufferedReader(reader);
		assertEquals("", bufReader.readLine().trim());
		assertEquals("<html>", bufReader.readLine());
		assertNull(bufReader.readLine());
	}
}

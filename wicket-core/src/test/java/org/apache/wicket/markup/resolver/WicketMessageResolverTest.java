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
package org.apache.wicket.markup.resolver;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.string.StringValueConversionException;
import org.junit.Test;

/**
 * 
 */
public class WicketMessageResolverTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void test_1() throws Exception
	{
		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
		executeTest(SimplePage_1.class, "SimplePageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_2() throws Exception
	{
		executeTest(SimplePage_2.class, "SimplePageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_2a() throws Exception
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(true);
		executeTest(SimplePage_2.class, "SimplePageExpectedResult_2a.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_3() throws Exception
	{
		executeTest(SimplePage_3.class, "SimplePageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_4() throws Exception
	{
		try
		{
			tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(true);
			tester.startPage(SimplePage_4.class);
			fail("Expected a WicketRuntimeException to happen");
		}
		catch (WicketRuntimeException ex)
		{
			String text = "Property 'myKey' not found";
			assertEquals(text, ex.getMessage().substring(0, text.length()));
			return;
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_5() throws Exception
	{
		executeTest(SimplePage_5.class, "SimplePageExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test_6() throws Exception
	{
		executeTest(SimplePage_6.class, "SimplePageExpectedResult_6.html");
	}

	@Test
	public void shouldEscapeMessageWhenEscapeAttributeIsSetToTrue() throws Exception
	{
		executeTest(WicketMessageResolverEscapePage.class, "WicketMessageResolverEscapePageExpectedResult.html");
	}

	@Test
	public void shouldFailWithEscapeSetToUnsupportedValue()
	{

		try 
		{
			tester.startPage(WicketMessageResolverEscapePageWrongEscapeValue.class);
			fail("Expected to fail: wrong escape value");
		}
		catch (StringValueConversionException ex)
		{
			String text = "Boolean value \"yesPlease\" not recognized";
			assertEquals(text, ex.getMessage());
		}
	}

}

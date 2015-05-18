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
package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.MarkupException;
import org.junit.Test;

/**
 * Simple application that demonstrates the mock http application code (and checks that it is
 * working)
 * 
 * @author Chris Turner
 */
public class HeaderSectionTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		executeTest(HeaderSectionPage_1.class, "HeaderSectionPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		executeTest(HeaderSectionPage_2.class, "HeaderSectionPageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_3() throws Exception
	{
		executeTest(HeaderSectionPage_3.class, "HeaderSectionPageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_4() throws Exception
	{
		executeTest(HeaderSectionPage_4.class, "HeaderSectionPageExpectedResult_4.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_5() throws Exception
	{
		executeTest(HeaderSectionPage_5.class, "HeaderSectionPageExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_6() throws Exception
	{
		executeTest(HeaderSectionPage_6.class, "HeaderSectionPageExpectedResult_6.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_7() throws Exception
	{
		executeTest(HeaderSectionPage_7.class, "HeaderSectionPageExpectedResult_7.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_8() throws Exception
	{
		executeTest(HeaderSectionPage_8.class, "HeaderSectionPageExpectedResult_8.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_9() throws Exception
	{
		executeTest(HeaderSectionPage_9.class, "HeaderSectionPageExpectedResult_9.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_9a() throws Exception
	{
		executeTest(HeaderSectionPage_9a.class, "HeaderSectionPageExpectedResult_9a.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_10() throws Exception
	{
		executeTest(HeaderSectionPage_10.class, "HeaderSectionPageExpectedResult_10.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_11() throws Exception
	{
		executeTest(HeaderSectionPage_11.class, "HeaderSectionPageExpectedResult_11.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_13() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(HeaderSectionPage_13.class, "HeaderSectionPageExpectedResult_13.html");
		}
		catch (WicketRuntimeException ex)
		{
			hit = true;
		}
		assertTrue("Expected a MarkupException to be thrown", hit);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_14() throws Exception
	{
		executeTest(HeaderSectionPage_14.class, "HeaderSectionPageExpectedResult_14.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_15() throws Exception
	{
		executeTest(HeaderSectionPage_15.class, "HeaderSectionPageExpectedResult_15.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_16() throws Exception
	{
		executeTest(HeaderSectionPage_16.class, "HeaderSectionPageExpectedResult_16.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_17() throws Exception
	{
		executeTest(HeaderSectionPage_17.class, "HeaderSectionPageExpectedResult_17.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_18() throws Exception
	{
		executeTest(HeaderSectionPage_18.class, "HeaderSectionPageExpectedResult_18.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_19() throws Exception
	{
		executeTest(HeaderSectionPage_19.class, "HeaderSectionPageExpectedResult_19.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_20() throws Exception
	{
		executeTest(HeaderSectionPage_20.class, "HeaderSectionPageExpectedResult_20.html");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5908
	 */
	@Test(expected = MarkupException.class)
	public void doubleHeadTagPage()
	{
		tester.startPage(DoubleHeadTagPage.class);
	}
}

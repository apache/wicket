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
package org.apache.wicket.markup;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 */
public class WicketNamespaceTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		executeTest(WicketNamespace_1.class, "WicketNamespaceExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		executeTest(WicketNamespace_2.class, "WicketNamespaceExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_3() throws Exception
	{
		executeTest(WicketNamespace_3.class, "WicketNamespaceExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_4() throws Exception
	{
		executeTest(WicketNamespace_4.class, "WicketNamespaceExpectedResult_4.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_5() throws Exception
	{
		executeTest(WicketNamespace_5.class, "WicketNamespaceExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_6() throws Exception
	{
		executeTest(WicketNamespace_6.class, "WicketNamespaceExpectedResult_6.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void doctype_1() throws Exception
	{
		executeTest(Doctype_1.class, "DoctypeExpectedResult_1.html");
		MarkupResourceStream rs = MarkupFactory.get()
			.getMarkup(tester.getLastRenderedPage(), true)
			.getMarkupResourceStream();
		assertEquals("html", rs.getDoctype());
		assertEquals(true, rs.isHtml5());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void doctype_InheritedPage() throws Exception
	{
		executeTest(Doctype_1_InheritedPage.class, "DoctypeExpectedResult_1_Inherited.html");
		MarkupResourceStream rs = MarkupFactory.get()
			.getMarkup(tester.getLastRenderedPage(), true)
			.getMarkupResourceStream();
		assertEquals("html", rs.getDoctype());
		assertEquals(true, rs.isHtml5());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void doctype_2() throws Exception
	{
		executeTest(Doctype_2.class, "DoctypeExpectedResult_2.html");
		MarkupResourceStream rs = MarkupFactory.get()
			.getMarkup(tester.getLastRenderedPage(), true)
			.getMarkupResourceStream();
		assertEquals(
			rs.getDoctype(),
			"html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"");
		assertEquals(false, rs.isHtml5());
	}
}

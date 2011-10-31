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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.settings.IMarkupSettings;
import org.junit.Test;

/**
 * Test the component: PageView
 * 
 * @author Juergen Donnerstag
 */
public class BoxBorderTest extends WicketTestCase
{

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test1() throws Exception
	{
		executeTest(BoxBorderTestPage_1.class, "BoxBorderTestPage_ExpectedResult_1.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception
	{
		executeTest(BoxBorderTestPage_2.class, "BoxBorderTestPage_ExpectedResult_2.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void test3() throws Exception
	{
		executeTest(BoxBorderTestPage_3.class, "BoxBorderTestPage_ExpectedResult_3.html");

		Border border = (Border)tester.getLastRenderedPage().get("border");
		assertNotNull(border);
		Form<?> form = (Form<?>)tester.getLastRenderedPage().get("border:myForm");

		TextField<String> input = (TextField<String>)tester.getLastRenderedPage().get(
			"border:myForm:border_body:name");
		assertEquals("", input.getDefaultModelObjectAsString());

		tester.getRequest().getPostParameters().setParameterValue(input.getInputName(), "jdo");
		tester.submitForm(form.getPageRelativePath());

		input = (TextField<String>)tester.getLastRenderedPage().get(
			"border:myForm:border_body:name");
		assertEquals("jdo", input.getDefaultModelObjectAsString());
	}

	/**
	 * Test to ensure MarkupException is thrown when Markup and Object hierarchy does not match with
	 * a Border involved.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test4() throws Exception
	{
		Class<? extends Page> pageClass = BorderTestHierarchyPage_4.class;

		System.out.println("=== " + pageClass.getName() + " ===");

		MarkupException markupException = null;
		try
		{
			tester.startPage(pageClass);
		}
		catch (MarkupException e)
		{
			markupException = e;
		}

		assertNotNull("Markup does not match component hierarchy, but exception not thrown.",
			markupException);
	}

	/**
	 * Test to ensure border render wrapped settings functions properly.
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderWrapped() throws Exception
	{
		executeTest(BorderRenderWrappedTestPage_1.class,
			"BorderRenderWrappedTestPage_ExpectedResult_1.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test5() throws Exception
	{
		executeTest(BoxBorderTestPage_5.class, "BoxBorderTestPage_ExpectedResult_5.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test6() throws Exception
	{
		executeTest(BoxBorderTestPage_6.class, "BoxBorderTestPage_ExpectedResult_6.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test7() throws Exception
	{
		final IMarkupSettings markupSettings = Application.get().getMarkupSettings();
		markupSettings.setCompressWhitespace(true);
		markupSettings.setStripComments(true);
		markupSettings.setStripWicketTags(true);

		executeTest(BoxBorderTestPage_1.class, "BoxBorderTestPage_ExpectedResult_7.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test8() throws Exception
	{
		executeTest(BoxBorderTestPage_8.class, "BoxBorderTestPage_ExpectedResult_8.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test9() throws Exception
	{
		executeTest(BoxBorderTestPage_9.class, "BoxBorderTestPage_ExpectedResult_9.html");
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = WicketRuntimeException.class)
	public void test10() throws Exception
	{
		executeTest(BoxBorderTestPage_10.class, "BoxBorderTestPage_ExpectedResult_10.html");
	}

	/**
	 * Test a simply page containing the debug component
	 * 
	 * @throws Exception
	 */
	@Test
	public void test11() throws Exception
	{
		executeTest(BoxBorderTestPage_11.class, "BoxBorderTestPage_ExpectedResult_11.html");

		Page page = tester.getLastRenderedPage();
		tester.clickLink("border:title");

		tester.clickLink("border:title");

		tester.clickLink("border:title");
	}
}

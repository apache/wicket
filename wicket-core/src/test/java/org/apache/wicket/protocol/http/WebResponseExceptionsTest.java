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
package org.apache.wicket.protocol.http;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.PageExpiredErrorPage;
import org.apache.wicket.settings.def.ExceptionSettings;
import org.apache.wicket.settings.def.RequestCycleSettings;
import org.junit.Test;


/**
 * Test exceptions thrown during request
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class WebResponseExceptionsTest extends WicketTestCase
{
	/**
	 * Tests buffered exception error page.
	 */
	@Test
	public void bufferedExceptionErrorPage()
	{
		tester.getApplication()
			.getRequestCycleSettings()
			.setRenderStrategy(RequestCycleSettings.RenderStrategy.REDIRECT_TO_BUFFER);
		tester.getApplication()
			.getExceptionSettings()
			.setUnexpectedExceptionDisplay(ExceptionSettings.SHOW_EXCEPTION_PAGE);
		internalErrorPage();
	}

	/**
	 * Tests exception error page.
	 */
	@Test
	public void exceptionErrorPage()
	{
		tester.getApplication()
			.getExceptionSettings()
			.setUnexpectedExceptionDisplay(ExceptionSettings.SHOW_EXCEPTION_PAGE);
		internalErrorPage();
	}

	/**
	 * Tests page expired.
	 */
	@Test
	public void expirePage()
	{
		tester.startPage(TestExpirePage.class);
		String document = tester.getLastResponseAsString();
		assertTrue(document.contains("Click me to get an error"));

		Link<?> link = (Link<?>)tester.getComponentFromLastRenderedPage("link");
		String linkUrl = tester.urlFor(link);

		// Clear the session to remove the pages
		tester.getSession().invalidateNow();

		// Invoke the call back URL of the ajax event behavior
		tester.setExposeExceptions(false);
		tester.executeUrl(linkUrl);
		assertEquals(PageExpiredErrorPage.class, tester.getLastRenderedPage().getClass());
	}

	/**
	 * Tests internal error page.
	 */
	@Test
	public void internalErrorPage()
	{
		tester.startPage(TestErrorPage.class);
		tester.setExposeExceptions(false);
		AjaxLink<?> link = (AjaxLink<?>)tester.getComponentFromLastRenderedPage("link");

		tester.executeAjaxEvent(link, "onclick");
		assertEquals(500, tester.getLastResponse().getStatus());
	}
}

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
package org.apache.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.util.tester.WicketTester;

import junit.framework.TestCase;

/**
 * Base class for tests which require comparing org.apache.wicket response with a file.
 * <p>
 * To create/replace the expected result file with the new content, define the
 * system property like -Dwicket.replace.expected.results=true
 * 
 */
public abstract class WicketTestCase extends TestCase
{
	/** */
	public WicketTester tester;

	/**
	 * Constructor
	 */
	public WicketTestCase()
	{
	}

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public WicketTestCase(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		tester = new WicketTester();
	}

	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * Use <code>-Dwicket.replace.expected.results=true</code> to
	 * automatically replace the expected output file.
	 * 
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	protected void executeTest(final Class pageClass, final String filename) throws Exception
	{
		System.out.println("=== " + pageClass.getName() + " ===");

		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		tester.assertResultPage(this.getClass(), filename);
	}

	/**
	 * 
	 * @param pageClass
	 * @param component
	 * @param filename
	 * @throws Exception
	 */
	protected void executedListener(final Class pageClass, final Component component,
			final String filename) throws Exception
	{
		assertNotNull(component);

		System.out.println("=== " + pageClass.getName() + " : " + component.getPageRelativePath()
				+ " ===");

		tester.executeListener(component);
		tester.assertResultPage(pageClass, filename);
	}

	/**
	 * 
	 * @param pageClass
	 * @param behavior
	 * @param filename
	 * @throws Exception
	 */
	protected void executedBehavior(final Class pageClass, final AbstractAjaxBehavior behavior,
			final String filename) throws Exception
	{
		assertNotNull(behavior);

		System.out.println("=== " + pageClass.getName() + " : " + behavior.toString() + " ===");

		tester.executeBehavior(behavior);
		tester.assertResultPage(pageClass, filename);
	}

	protected String getContentType()
	{
		return ((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse())
				.getHeader("Content-Type");
	}

	protected int getContentLength()
	{
		String contentLength = ((MockHttpServletResponse)tester.getWicketResponse()
				.getHttpServletResponse()).getHeader("Content-Length");
		if (contentLength == null)
			throw new WicketRuntimeException("No Content-Length header found");
		return Integer.parseInt(contentLength);
	}

	protected String getLastModified()
	{
		return ((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse())
				.getHeader("Last-Modified");
	}

	protected String getContentDisposition()
	{
		return ((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse())
				.getHeader("Content-Disposition");
	}

	protected void assertAjaxLocation()
	{
		assertNull("Location header should *not* be present when using Ajax",
				((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse())
						.getRedirectLocation());
		String ajaxLocation = ((MockHttpServletResponse)tester.getWicketResponse()
				.getHttpServletResponse()).getHeader("Ajax-Location");
		assertNotNull("Ajax-Location header should be present when using Ajax", ajaxLocation);
		int statusCode = ((MockHttpServletResponse)tester.getWicketResponse()
				.getHttpServletResponse()).getStatus();
		assertEquals(200, statusCode);
	}
}

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
package org.apache.wicket.request.target.coding;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.util.lang.PackageName;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Tests package resources.
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class UrlMountingTest extends TestCase
{
	private WicketTester tester;

	/**
	 * Tests mounting.
	 */
	public void testBadRequest1()
	{
		tester.getServletRequest().setPath("/mount/XXXpoint");
		assertNull(getRequestCodingStrategy());
	}

	/**
	 * Tests mounting.
	 */
	public void testBadRequest2()
	{
		tester.getServletRequest().setPath("/mount/pointXXX");
		assertNull(getRequestCodingStrategy());
	}

	/**
	 * Tests mounting.
	 */
	public void testBadRequest3()
	{
		tester.getServletRequest().setPath("/mount/point/nonexistent.TestPage");
		IRequestTargetUrlCodingStrategy ucs = getRequestCodingStrategy();
		assertNotNull(ucs);
		try
		{
			ucs.decode(tester.getWicketRequest().getRequestParameters());
			fail("decode() should have raised a WicketRuntimeException!");
		}
		catch (WicketRuntimeException e)
		{
			assertEquals(
					"Unable to load class with name: org.apache.wicket.request.target.coding.nonexistent.TestPage",
					e.getMessage());
		}
	}

	/**
	 * Test direct access (with wicket parameters) to a mounted page that should
	 * be allowed. By default, enforcement is not turned on, so we don't set it
	 * as a setting here.
	 */
	public void testDirectAccessToMountedPageAllowed()
	{
		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL(
				"?wicket:bookmarkablePage=:" + TestPage.class.getName() + "");
		tester.processRequestCycle();
		tester.assertRenderedPage(TestPage.class);
	}

	/**
	 * Test direct access (with wicket parameters) to a mounted page that should
	 * NOT be allowed due to the {@link ISecuritySettings#getEnforceMounts()}
	 * setting being set to true.
	 */
	public void testDirectAccessToMountedPageNotAllowed()
	{
		tester.getApplication().getSecuritySettings().setEnforceMounts(true);

		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL(
				"?wicket:bookmarkablePage=:" + TestPage.class.getName() + "");
		try
		{
			tester.processRequestCycle();
			fail("This request should not have been allowed");
		}
		catch (AbortWithWebErrorCodeException e)
		{
			assertEquals(e.getErrorCode(), HttpServletResponse.SC_FORBIDDEN);
		}
		finally
		{
			tester.getApplication().getSecuritySettings().setEnforceMounts(false);
		}
	}

	/**
	 * Test direct access (with wicket parameters) to a mounted page including
	 * (part of the) mount path.
	 * 
	 * @see WebRequestCycleProcessor#resolve(org.apache.wicket.RequestCycle,
	 *      org.apache.wicket.request.RequestParameters) for an explanation of
	 *      this test
	 */
	public void testDirectAccessToMountedPageWithExtraPath()
	{
		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL(
				"/foo/bar/?wicket:bookmarkablePage=:" + TestPage.class.getName() + "");
		tester.processRequestCycle();
		tester.assertRenderedPage(TestPage.class);
	}

	/**
	 * Test mount access to a mounted page that should be allowed.
	 */
	public void testMountAccessToMountedPageAllowed()
	{
		tester.getApplication().getSecuritySettings().setEnforceMounts(false);

		tester.setupRequestAndResponse();
		tester.getServletRequest().setURL("/mount/point/TestPage");
		tester.processRequestCycle();
		tester.assertRenderedPage(TestPage.class);
	}

	/**
	 * Tests mounting.
	 */
	public void testValidMount1()
	{
		tester.getServletRequest().setPath("/mount/point");
		IRequestTargetUrlCodingStrategy ucs = getRequestCodingStrategy();
		assertNotNull(ucs);
		assertNull(ucs.decode(tester.getWicketRequest().getRequestParameters()));
	}

	/**
	 * Tests mounting.
	 */
	public void testValidMount2()
	{
		tester.getServletRequest().setPath("/mount/point/TestPage");
		IRequestTargetUrlCodingStrategy ucs = getRequestCodingStrategy();
		assertNotNull(ucs);
		assertNotNull(ucs.decode(tester.getWicketRequest().getRequestParameters()));
	}

	/**
	 * @return request coding strategy for this test.
	 */
	private IRequestTargetUrlCodingStrategy getRequestCodingStrategy()
	{
		String relativePath = tester.getApplication().getWicketFilter().getRelativePath(
				tester.getServletRequest());
		return tester.getApplication().getRequestCycleProcessor().getRequestCodingStrategy()
				.urlCodingStrategyForPath(relativePath);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		tester = new WicketTester();
		tester.getApplication().mount("/mount/point", PackageName.forClass(TestPage.class));
		tester.setupRequestAndResponse();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		tester.destroy();
	}
}

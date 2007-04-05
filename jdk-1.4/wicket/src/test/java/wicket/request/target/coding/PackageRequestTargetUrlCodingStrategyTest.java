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
package wicket.request.target.coding;

import junit.framework.TestCase;
import wicket.WicketRuntimeException;
import wicket.util.lang.PackageName;
import wicket.util.tester.WicketTester;

/**
 * Tests package resources.
 */
public class PackageRequestTargetUrlCodingStrategyTest extends TestCase
{
	WicketTester tester;

	/**
	 * Tests mounting.
	 */
	public void test1()
	{
		tester.getServletRequest().setPath("/mount/XXXpoint");
		assertNull(getRequestCodingStrategy());
	}

	/**
	 * Tests mounting.
	 */
	public void test2()
	{
		tester.getServletRequest().setPath("/mount/pointXXX");
		assertNull(getRequestCodingStrategy());
	}

	/**
	 * Tests mounting.
	 */
	public void test3()
	{
		tester.getServletRequest().setPath("/mount/point");
		IRequestTargetUrlCodingStrategy ucs = getRequestCodingStrategy();
		assertNotNull(ucs);
		assertNull(ucs.decode(tester.getWicketRequest().getRequestParameters()));
	}

	/**
	 * Tests mounting.
	 */
	public void test4()
	{
		tester.getServletRequest().setPath("/mount/point/TestPage");
		IRequestTargetUrlCodingStrategy ucs = getRequestCodingStrategy();
		assertNotNull(ucs);
		assertNotNull(ucs.decode(tester.getWicketRequest().getRequestParameters()));
	}

	/**
	 * Tests mounting.
	 */
	public void test5()
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
					"Unable to load class with name: wicket.request.target.coding.nonexistent.TestPage",
					e.getMessage());
		}
	}

	protected void setUp() throws Exception
	{
		tester = new WicketTester();
		tester.getApplication().mount("/mount/point", PackageName.forClass(TestPage.class));
		tester.setupRequestAndResponse();
	}

	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	IRequestTargetUrlCodingStrategy getRequestCodingStrategy()
	{
		String relativePath = tester.getApplication().getWicketFilter().getRelativePath(
				tester.getServletRequest());
		return tester.getApplication().getRequestCycleProcessor().getRequestCodingStrategy()
				.urlCodingStrategyForPath(relativePath);
	}
}

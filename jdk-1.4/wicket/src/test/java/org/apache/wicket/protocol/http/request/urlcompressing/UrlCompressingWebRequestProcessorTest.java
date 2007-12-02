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
package org.apache.wicket.protocol.http.request.urlcompressing;

import junit.framework.TestCase;

import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.tester.WicketTester.DummyWebApplication;

/**
 * @author Juergen Donnerstag
 */
public class UrlCompressingWebRequestProcessorTest extends TestCase
{
	private WicketTester tester;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();

		tester = new WicketTester(new DummyWebApplication()
		{
			/**
			 * Special overwrite to have url compressing for this example.
			 */
			protected IRequestCycleProcessor newRequestCycleProcessor()
			{
				return new UrlCompressingWebRequestProcessor();
			}
		});
	}

	/**
	 * 
	 */
	public void test1()
	{
		tester.startPage(MyPage.class);
	}

	/**
	 * 
	 */
	public void test2()
	{
		// @TODO Johan, why does this fail with a NumberException????
		// tester.startPage(new MyPage());
	}

	/**
	 * 
	 */
	public void test3()
	{
		// @TODO Johan, why does this fail with a NumberException????
// tester.startPage(new ITestPageSource()
// {
// private static final long serialVersionUID = 1L;
//
// public Page getTestPage()
// {
// PageParameters params = new PageParameters();
// params.add("0", "param");
// return new MyPage();
// }
// });
	}
}

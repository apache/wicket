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
package org.apache.wicket.redirect.abort;

import junit.framework.Assert;

import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;

/**
 * Testcase for WICKET-1418, throwing an abortexception during rendering.
 * 
 * @author Peter Ertl
 * @author marrink
 * 
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-1418">WICKET-1418</a>
 */
public class AbortExceptionTest extends WicketTestCase
{
	/**
	 * Test page without throwing abort.
	 */
	public void testNoAbort()
	{
		System.out.println("testing good usecase");
		tester.processRequestCycle(AbortExceptionPage.class, new PageParameters("trigger=false"));
		Assert.assertEquals(1234, tester.getServletResponse().getCode());
	}

	/**
	 * Test page with throwing abort.
	 */
	public void testAbort()
	{
		try
		{

			System.out.println("testing bad usecase");
			tester.processRequestCycle(AbortExceptionPage.class, new PageParameters("trigger=true"));
			Assert.assertEquals(1234, tester.getServletResponse().getCode()); // this will fail
		}
		catch (WicketRuntimeException x)
		{
			final Throwable reason = x.getCause();

			Assert.assertEquals(reason.getClass(), AbortWithWebErrorCodeException.class);
			Assert.fail("this must not happen (we expect a redirect happen here and handled by wicket request processor)");
		}
	}

}

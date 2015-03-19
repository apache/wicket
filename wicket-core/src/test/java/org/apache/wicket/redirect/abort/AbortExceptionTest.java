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

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Assert;
import org.junit.Test;

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
	@Test
	public void testNoAbort()
	{
		tester.startPage(AbortExceptionPage.class, new PageParameters().set("trigger", false));
		Assert.assertEquals(1234, tester.getLastResponse().getStatus());
	}

	/**
	 * Test page with throwing abort.
	 */
	@Test
	public void testAbort()
	{
		try
		{
			tester.startPage(AbortExceptionPage.class, new PageParameters().set("trigger", true));
			Assert.assertEquals(1234, tester.getLastResponse().getStatus()); // this will
			// fail
		}
		catch (RuntimeException x)
		{
			final Throwable reason = x.getCause();

			Assert.assertEquals(reason.getClass(), AbortWithHttpErrorCodeException.class);
			Assert.fail("this must not happen (we expect a redirect happen here and handled by wicket request processor)");
		}
	}

}

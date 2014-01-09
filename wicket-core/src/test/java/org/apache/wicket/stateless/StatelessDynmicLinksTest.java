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
package org.apache.wicket.stateless;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.Url;
import org.junit.Test;

/**
 * @author svenmeier
 */
public class StatelessDynmicLinksTest extends WicketTestCase
{
	/**
	 * WICKET-5460
	 */
	@Test
	public void foo() throws Exception
	{
		tester.getApplication().mountPage("/stateless", StatelessPageWithDynamicLinks.class);

		tester.startPage(StatelessPageWithDynamicLinks.class);
		assertTrue(tester.getLastResponseAsString().contains("LINK-1"));

		tester.getRequest().setUrl(Url.parse("stateless?-0.ILinkListener-links:1"));
		tester.processRequest();
		assertTrue(tester.getLastResponseAsString().contains("LINK-2"));

		tester.getRequest().setUrl(Url.parse("stateless?-0.ILinkListener-links:2"));
		tester.processRequest();
		assertTrue(tester.getLastResponseAsString().contains("LINK-3"));

		tester.getRequest().setUrl(Url.parse("stateless?-0.ILinkListener-links:3"));
		tester.processRequest();
		assertTrue(tester.getLastResponseAsString().contains("LINK-4"));
	}
}

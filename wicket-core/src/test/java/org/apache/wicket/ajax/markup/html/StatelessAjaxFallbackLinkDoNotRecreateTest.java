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
package org.apache.wicket.ajax.markup.html;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.After;
import org.junit.Test;

public class StatelessAjaxFallbackLinkDoNotRecreateTest extends WicketTestCase
{
	@After
	public void after()
	{
		// things must stay stateless
		assertTrue(Session.get().isTemporary());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6349
	 */
	@Test
	public void statelessPagesAreAlwaysRecreated()
	{
		tester.getApplication().getPageSettings().setRecreateBookmarkablePagesAfterExpiry(false);
		tester.startPage(StatelessAjaxFallbackLinkDoNotRecreatePage.class);

		final Page page = tester.getLastRenderedPage();
		assertTrue(page.isStateless());

		tester.clickLink("incrementLink");

		tester.assertRenderedPage(StatelessAjaxFallbackLinkDoNotRecreatePage.class);
	}
}

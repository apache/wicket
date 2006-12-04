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
package wicket.markup.html.internal;

import wicket.WicketTestCase;

/**
 * Test for the {@link wicket.markup.html.internal.HtmlHeaderContainer}.
 * 
 * @author svenmeier
 */
public class HtmlHeaderContainerTest extends WicketTestCase
{
	/**
	 * Construct.
	 * @param name
	 */
	public HtmlHeaderContainerTest(String name)
	{
		super(name);
	}

	/**
	 * Test clean-up of auto added components after removal of the
	 * HtmlHeaderContainer.
	 */
	public void testCleanUpOfAutoAddedComponents()
	{
		application.setHomePage(ComponentResolvingPage.class);
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// onEndRequest() of auto added component was not called in
		// MarkupContainer#internalEndRequest() using an iterator
		ComponentResolvingPage page = (ComponentResolvingPage)application.getLastRenderedPage();
		assertTrue("onEndRequest() should be called",
				page.onEndRequestWasCalledOnAutoAddedComponent);
	}
}

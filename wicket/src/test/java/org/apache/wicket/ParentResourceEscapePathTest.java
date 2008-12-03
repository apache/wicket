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

import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;

public class ParentResourceEscapePathTest extends WicketTestCase
{
	public void testParentEscapeSequenceInRenderedHtml() throws Exception
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		parentEscapeSequenceInRenderedHtml();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		parentEscapeSequenceInRenderedHtml();
	}

	private void parentEscapeSequenceInRenderedHtml()
	{
		tester.setupRequestAndResponse();
		tester.startPage(ParentResourceEscapePathTestPage.class);
		tester.assertRenderedPage(ParentResourceEscapePathTestPage.class);
		tester.assertNoErrorMessage();

		String html = tester.getServletResponse().getDocument();
		assertContains(html, "<html><head><wicket:link><script ");
		assertContains(html, " type=\"text/javascript\"");
		assertContains(html, "src=\"" + expectedResourceUrl() + "\"");
		assertContains(html, "\"></script></wicket:link></head></html>");
	}

	private void assertContains(String html, String expected)
	{
		assertTrue(html, html.contains(expected));
	}

	public void testResourceUrlGeneratedByResourceReference()
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		resourceUrlGeneratedByResourceReference();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		resourceUrlGeneratedByResourceReference();
	}

	private void resourceUrlGeneratedByResourceReference()
	{
		final ResourceReference ref = new ResourceReference(ParentResourceEscapePathTestPage.class,
			"../../../ParentResourceTest.js");

		assertEquals(expectedResourceUrl(), tester.createRequestCycle().urlFor(ref).toString());
	}

	public void testRequestHandlingOfResourceUrlWithEscapeStringInside()
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		requestHandlingOfResourceUrlWithEscapeStringInside();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		requestHandlingOfResourceUrlWithEscapeStringInside();
	}

	private void requestHandlingOfResourceUrlWithEscapeStringInside()
	{
		final WebRequestCycle cycle = tester.setupRequestAndResponse();
		final MockHttpServletRequest request = tester.getServletRequest();
		request.setMethod("GET");
		request.setURL("http://localhost/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication/" +
			expectedResourceUrl());
		tester.processRequestCycle(cycle);
		tester.assertNoErrorMessage();
		assertEquals("// ParentResourceTest.js", tester.getServletResponse().getDocument());
	}

	private String expectedResourceUrl()
	{
		final CharSequence escapeSequence = tester.getApplication()
			.getResourceSettings()
			.getParentFolderPlaceholder();

		final StringBuilder url = new StringBuilder();
		url.append("resources/org.apache.wicket.ParentResourceEscapePathTestPage/");

		for (int i = 0; i < 3; i++)
		{
			url.append(escapeSequence);
			url.append('/');
		}
		url.append("ParentResourceTest.js");

		return url.toString();
	}
}

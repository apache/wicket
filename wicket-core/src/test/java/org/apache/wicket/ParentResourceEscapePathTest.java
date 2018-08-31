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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.apache.wicket.markup.html.PackageResourceGuard;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
class ParentResourceEscapePathTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(ParentResourceEscapePathTest.class);

	/**
	 * @throws Exception
	 */
	@Test
	void parentEscapeSequenceInRenderedHtmlTest() throws Exception
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		parentEscapeSequenceInRenderedHtml();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		parentEscapeSequenceInRenderedHtml();
	}

	private void parentEscapeSequenceInRenderedHtml()
	{
		tester.startPage(ParentResourceEscapePathTestPage.class);
		tester.assertRenderedPage(ParentResourceEscapePathTestPage.class);
		tester.assertNoErrorMessage();

		log.error(tester.getLastResponseAsString());

		String html = tester.getLastResponseAsString();
		assertContains(html, "<html><head><wicket:link><script ");
		assertContains(html, " type=\"text/javascript\"");
		assertContains(html, expectedResourceUrl() + "\"");
		assertContains(html, "\"></script></wicket:link></head></html>");
	}

	private void assertContains(String html, String expected)
	{
		assertTrue(html.contains(expected), "Expected to find \"" + expected + "\" in \"" + html + "\"");
	}

	/**
	 * testResourceUrlGeneratedByResourceReference()
	 */
	@Test
	void resourceUrlGeneratedByResourceReferenceTest()
	{
		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		resourceUrlGeneratedByResourceReference();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		resourceUrlGeneratedByResourceReference();
	}

	private void resourceUrlGeneratedByResourceReference()
	{
		final ResourceReference ref = new PackageResourceReference(
			ParentResourceEscapePathTestPage.class, "../../../ParentResourceTest.js");

		assertContains(tester.getRequestCycle().mapUrlFor(ref, null).toString(),
			expectedResourceUrl());
	}

	/**
	 * testRequestHandlingOfResourceUrlWithEscapeStringInside()
	 */
	@Test
	void requestHandlingOfResourceUrlWithEscapeStringInsideTest()
	{
		((PackageResourceGuard)tester.getApplication()
			.getResourceSettings()
			.getPackageResourceGuard()).setAllowAccessToRootResources(true);

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("-updir-");
		requestHandlingOfResourceUrlWithEscapeStringInside();

		tester.getApplication().getResourceSettings().setParentFolderPlaceholder("$up$");
		requestHandlingOfResourceUrlWithEscapeStringInside();
	}

	private void requestHandlingOfResourceUrlWithEscapeStringInside()
	{
		tester.getRequest().setUrl(Url.parse("wicket/" + expectedResourceUrl()));
		InputStream i = getClass().getClassLoader().getResourceAsStream("ParentResourceTest.js");
		i = getClass().getClassLoader().getResourceAsStream("/ParentResourceTest.js");

		tester.processRequest();
		tester.assertNoErrorMessage();

		String res = tester.getLastResponse().getBinaryResponse();
		assertEquals("// ParentResourceTest.js", res);
	}

	private String expectedResourceUrl()
	{
		final CharSequence escapeSequence = tester.getApplication()
			.getResourceSettings()
			.getParentFolderPlaceholder();

		final StringBuilder url = new StringBuilder();
		url.append("resource/org.apache.wicket.ParentResourceEscapePathTestPage/");

		for (int i = 0; i < 3; i++)
		{
			url.append(escapeSequence);
			url.append('/');
		}
		url.append("ParentResourceTest.js");

		return url.toString();
	}
}

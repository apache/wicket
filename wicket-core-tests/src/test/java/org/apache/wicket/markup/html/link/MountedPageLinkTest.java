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
package org.apache.wicket.markup.html.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.core.request.mapper.PageInstanceMapper;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testcases for links on mounted pages. These links are special, because they refer the page by id
 * AND by mount path (including parameters). This was done for WICKET-4014. WICKET-4290 broke this,
 * because the page parameters are no longer rendered.
 * 
 * @author papegaaij
 */
class MountedPageLinkTest extends WicketTestCase
{


	private void mountPage(boolean argument)
	{
		if (argument) {
			tester.getApplication().mountPage("mount/${param}/part2", PageWithLink.class);
		}
	}

	/**
	 * Tests if the page parameters are part of the url of the link, and if the link actually works.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void testPageParametersInLink(boolean doMount)
	{
		mountPage(doMount);

		PageWithLink page = tester.startPage(PageWithLink.class,
											 new PageParameters().add("param", "value"));
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		if (doMount)
			assertTrue(url.contains("mount/value/part2"),
					   "URL for link should contain 'mount/value/part2': " + url);
		else
			assertTrue(url.contains("param=value"),
					   "URL for link should contain 'param=value': " + url);
		tester.executeUrl(url);
	}

	/**
	 * Tests if it is possible to re-instantiate the page if it is expired. The page should be
	 * instantiated with the same page parameters. The link will not be clicked however.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void testLinkOnExpiredPage(boolean argument)
	{
		mountPage(argument);

		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value"));
		assertEquals("value", page.getPageParameters().get("param").toString());
		tester.assertContains("param=value");
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		// simulate a page expiry
		url = url.replace("?0", "?3");
		tester.executeUrl(url);

		tester.assertContains("param=value");
	}

	/**
	 * Tests if the {@link PageInstanceMapper} is used if
	 * {@link org.apache.wicket.settings.PageSettings#getRecreateBookmarkablePagesAfterExpiry()} is
	 * disabled
	 */
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void testLinkOnPageWithRecreationDisabled(boolean doMount)
	{
		mountPage(doMount);

		tester.getApplication().getPageSettings().setRecreateBookmarkablePagesAfterExpiry(false);
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value", INamedParameters.Type.MANUAL));
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		assertEquals(
			"./wicket/bookmarkable/org.apache.wicket.markup.html.link.PageWithLink?0-1.-link", url);
		tester.executeUrl(url);
	}

	/**
	 * ... and this should throw a {@link PageExpiredException} if the page is expired
	 */
	@ParameterizedTest
	@ValueSource(strings = { "true", "false" })
	void testExpiredPageWithRecreationDisabled(boolean doMount)
	{
		mountPage(doMount);

		tester.getApplication().getPageSettings().setRecreateBookmarkablePagesAfterExpiry(false);
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value", INamedParameters.Type.MANUAL));
		Link<?> link = (Link<?>)page.get("link");

		assertThrows(PageExpiredException.class, () -> {
			String url = link.getURL().toString();
			assertEquals(
				"./wicket/bookmarkable/org.apache.wicket.markup.html.link.PageWithLink?0-1.-link",
				url);
			// simulate a page expiry
			url = url.replace("PageWithLink?0", "PageWithLink?3");

			tester.executeUrl(url);
		});
	}
}

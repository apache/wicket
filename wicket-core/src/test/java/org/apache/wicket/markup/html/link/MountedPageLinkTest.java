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

import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.request.mapper.PageInstanceMapper;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Testcases for links on mounted pages. These links are special, because they refer the page by id
 * AND by mount path (including parameters). This was done for WICKET-4014. WICKET-4290 broke this,
 * because the page parameters are no longer rendered.
 * 
 * @author papegaaij
 */
@RunWith(Parameterized.class)
public class MountedPageLinkTest extends WicketTestCase
{
	@Parameters
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][] { { true }, { false } });
	}

	private boolean mount;

	public MountedPageLinkTest(boolean mount)
	{
		this.mount = mount;
	}

	/**
	 * Mount the page
	 */
	@Before
	public void mountPage()
	{
		if (mount)
			tester.getApplication().mountPage("mount/${param}/part2", PageWithLink.class);
	}

	/**
	 * Tests if the page parameters are part of the url of the link, and if the link actually works.
	 */
	@Test
	public void testPageParametersInLink()
	{
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value"));
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		if (mount)
			assertTrue("URL for link should contain 'mount/value/part2': " + url, url.toString()
				.contains("mount/value/part2"));
		else
			assertTrue("URL for link should contain 'param=value': " + url, url.toString()
				.contains("param=value"));
		tester.executeUrl(url);
	}

	/**
	 * Tests if it is possible to re-instantiate the page if it is expired. The page should be
	 * instantiated with the same page parameters. The link will not be clicked however.
	 */
	@Test
	public void testLinkOnExpiredPage()
	{
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value"));
		assertEquals("value", page.getPageParameters().get("param").toString());
		tester.assertContains("param=value");
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		// simulate a page expiry
		url = url.replace("?0", "?3");
		tester.executeUrl(url);

		// request parameters to callback urls should be ignored for the re-created page
		// (WICKET-4594)
		tester.assertContainsNot("param=value");
	}

	/**
	 * Tests if the {@link PageInstanceMapper} is used if
	 * {@link org.apache.wicket.settings.PageSettings#getRecreateMountedPagesAfterExpiry()}
	 * is disabled
	 */
	@Test
	public void testLinkOnPageWithRecreationDisabled()
	{
		tester.getApplication().getPageSettings().setRecreateMountedPagesAfterExpiry(false);
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value"));
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		assertEquals("./wicket/page?0-1.ILinkListener-link", url);
		tester.executeUrl(url);
	}

	/**
	 * ... and this should throw a {@link PageExpiredException} if the page is expired
	 */
	@Test(expected = PageExpiredException.class)
	public void testExpiredPageWithRecreationDisabled()
	{
		tester.getApplication().getPageSettings().setRecreateMountedPagesAfterExpiry(false);
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value"));
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		assertEquals("./wicket/page?0-1.ILinkListener-link", url);
		// simulate a page expiry
		url = url.replace("page?0", "page?3");
		tester.executeUrl(url);
	}
}

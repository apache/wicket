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

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Before;
import org.junit.Test;

public class MountedPageLinkTest extends WicketTestCase
{
	@Before
	public void mountPage()
	{
		tester.getApplication().mountPage("mount/${param}/part2", PageWithLink.class);
	}

	@Test
	public void testPageParametersInLink()
	{
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value"));
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		assertTrue("URL for link should contain 'mount/value/part2': " + url, url.toString()
			.contains("mount/value/part2"));
		tester.executeUrl(url);
	}

	@Test
	public void testLinkOnExpiredPage()
	{
		PageWithLink page = tester.startPage(PageWithLink.class,
			new PageParameters().add("param", "value"));
		Link<?> link = (Link<?>)page.get("link");
		String url = link.getURL().toString();
		url = url.replace("part2?0", "part2?3");
		tester.executeUrl(url);
	}
}

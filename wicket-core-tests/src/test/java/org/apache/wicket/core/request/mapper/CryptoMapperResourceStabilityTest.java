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
package org.apache.wicket.core.request.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies from the client's point of view that a {@link CryptoMapper} does not break browser
 * caching: the encrypted resource URLs a page renders must be identical every time the page is
 * rendered, otherwise the browser cannot recognise the resource it already holds and re-downloads
 * it on every page view, no matter how long-lived the cache headers are.
 */
class CryptoMapperResourceStabilityTest
{
	private static final Pattern SRC = Pattern.compile("src=\"([^\"]+)\"");

	private WicketTester tester;

	@BeforeEach
	void before()
	{
		tester = new WicketTester(new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				// the realistic setup: encrypt everything, with the default per-session key
				setRootRequestMapper(new CryptoMapper(getRootRequestMapper(), this));
			}
		});
	}

	@AfterEach
	void after()
	{
		tester.destroy();
	}

	@Test
	void renderedResourceUrlsAreIdenticalOnEveryRender()
	{
		tester.startPage(ResourcePage.class);
		List<String> first = resourceUrls(tester.getLastResponseAsString());

		tester.startPage(ResourcePage.class);
		List<String> second = resourceUrls(tester.getLastResponseAsString());

		assertFalse(first.isEmpty(), "expected the page to render a resource URL");
		// the URL is encrypted, not the plain resource path
		assertFalse(first.get(0).contains("crypt.txt"), "expected an encrypted URL");
		assertEquals(first, second);
	}

	private static List<String> resourceUrls(String markup)
	{
		List<String> urls = new ArrayList<>();
		Matcher matcher = SRC.matcher(markup);
		while (matcher.find())
		{
			urls.add(matcher.group(1));
		}
		return urls;
	}

	/** A page rendering a URL to a packaged resource. */
	public static class ResourcePage extends WebPage
	{
		public ResourcePage()
		{
			add(new Image("image",
				new PackageResourceReference(CryptoMapperTest.class, "crypt/crypt.txt")));
		}

		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body><img wicket:id=\"image\"/></body></html>");
		}
	}
}

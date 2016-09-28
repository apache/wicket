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
package org.apache.wicket.settings;

import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.https.HttpsConfig;
import org.apache.wicket.protocol.https.HttpsMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link SecuritySettings}
 */
public class ISecuritySettingsTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3849
	 */
	@Test
	public void enforceMounts()
	{
		MockPageWithLink pageWithLink = new MockPageWithLink();
		pageWithLink.add(new Link<Void>(MockPageWithLink.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				throw new RedirectToUrlException("/wicket/bookmarkable/" +
					UnknownPage.class.getName());
			}
		});

		tester.startPage(pageWithLink);
		tester.assertRenderedPage(MockPageWithLink.class);
		tester.clickLink(MockPageWithLink.LINK_ID);
		tester.assertRenderedPage(UnknownPage.class);

		tester.getApplication().getSecuritySettings().setEnforceMounts(true);

		tester.startPage(pageWithLink);
		tester.assertRenderedPage(MockPageWithLink.class);
		tester.clickLink(MockPageWithLink.LINK_ID);
		Assert.assertNull(tester.getLastRenderedPage());

		/*
		 * Test that mounts are enforced when the root compound mapper does not directly contain the mounted mapper.
		 */
		tester.getApplication().setRootRequestMapper(new HttpsMapper(tester.getApplication().getRootRequestMapper(), new HttpsConfig()));

		tester.startPage(pageWithLink);
		tester.assertRenderedPage(MockPageWithLink.class);
		tester.clickLink(MockPageWithLink.LINK_ID);
		Assert.assertNull(tester.getLastRenderedPage());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5560
	 */
	@Test
	public void enforceMountsWithCryptoMapper()
	{
		WebApplication app = tester.getApplication();

		IRequestHandler handler = new BookmarkablePageRequestHandler(new PageProvider(UnknownPage.class));

		String plainTextNonMountedUrl = tester.urlFor(handler).toString();

		assertTrue("Plain text non mounted url should start with wicket/bookmarkable/: " + plainTextNonMountedUrl, plainTextNonMountedUrl.startsWith("wicket/bookmarkable/"));

		tester.executeUrl(plainTextNonMountedUrl);
		tester.assertRenderedPage(UnknownPage.class);

		app.setRootRequestMapper(new CryptoMapper(app.getRootRequestMapper(), app));

		/*
		 * Execute dummy request to get WicketTester to re-initialise with CryptoMapper in place.
		 */
		tester.executeUrl("");

		String encryptedNonMountedUrl = tester.urlFor(handler).toString();

		assertFalse("Encrypted URL should not start with wicket/bookmarkable/" + encryptedNonMountedUrl, encryptedNonMountedUrl.startsWith("wicket/bookmarkable/"));

		tester.executeUrl(plainTextNonMountedUrl);
		assertNull(tester.getLastRenderedPage());
		tester.executeUrl(encryptedNonMountedUrl);
		tester.assertRenderedPage(UnknownPage.class);

		app.mountPackage("unknown", UnknownPage.class);

		tester.executeUrl(plainTextNonMountedUrl);
		assertNull(tester.getLastRenderedPage());
		tester.executeUrl(encryptedNonMountedUrl);
		tester.assertRenderedPage(UnknownPage.class);

		app.getSecuritySettings().setEnforceMounts(true);

		tester.executeUrl(plainTextNonMountedUrl);
		assertNull(tester.getLastRenderedPage());
		tester.executeUrl(encryptedNonMountedUrl);
		assertNull(tester.getLastRenderedPage());
	}
	
	/**
	 * Dummy page for testing BookmarkableMapper
	 */
	public static class UnknownPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body></body></html>");
		}
	}
}

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
package org.apache.wicket.markup.html.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * Page that let the browser redirect. Use this if you want to direct the browser to some external
 * URL, like Google etc. or if you want to redirect to a Wicket page, but with a delay.
 * 
 * @author Eelco Hillenius
 */
public class RedirectPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor. The page will immediately redirect to the given url.
	 * 
	 * @param url
	 *            The url to redirect to
	 */
	public RedirectPage(final CharSequence url)
	{
		this(url, 0);
	}

	/**
	 * Constructor. The page will redirect to the given url after waiting for the given number of
	 * seconds.
	 * 
	 * @param url
	 *            The url to redirect to
	 * @param waitBeforeRedirectInSeconds
	 *            The number of seconds the browser should wait before redirecting
	 */
	public RedirectPage(final CharSequence url, final int waitBeforeRedirectInSeconds)
	{
		final WebMarkupContainer redirect = new WebMarkupContainer("redirect");
		final String content = waitBeforeRedirectInSeconds + ";URL=" + url;
		redirect.add(new AttributeModifier("content", new Model<>(content)));
		add(redirect);
	}

	/**
	 * Construct. The page will redirect to the given Page.
	 * 
	 * @param page
	 *            The page to redirect to.
	 */
	public RedirectPage(final Page page)
	{
		this(page, 0);
	}

	/**
	 * Construct. The page will redirect to the given Page after waiting for the given number of
	 * seconds.
	 * 
	 * @param page
	 *            The page to redirect to.
	 * @param waitBeforeRedirectInSeconds
	 *            The number of seconds the browser should wait before redirecting
	 */
	public RedirectPage(final Page page, final int waitBeforeRedirectInSeconds)
	{
		this(page.urlFor(new RenderPageRequestHandler(new PageProvider(page))),
			waitBeforeRedirectInSeconds);
	}

	/**
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}

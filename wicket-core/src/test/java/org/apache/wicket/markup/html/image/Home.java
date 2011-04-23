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
package org.apache.wicket.markup.html.image;

import java.util.Locale;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Demonstrates localization.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public final class Home extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters (ignored since this is the home page)
	 */
	public Home(final PageParameters parameters)
	{
		add(new Image("logo", new PackageResourceReference(Home.class, "../../border/logo.gif")));
		add(new Image("beer", new PackageResourceReference(Home.class, "Beer.gif")));

		// Add a couple of links to be able to play around with the session
		// locale
		add(new Link<Void>("goCanadian")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().setLocale(Locale.CANADA);
			}
		});
		add(new Link<Void>("goUS")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().setLocale(Locale.US);
			}
		});
		add(new Link<Void>("goDutch")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("nl", "NL"));
			}
		});
		add(new Link<Void>("goGerman")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("de", "DE"));
			}
		});
		add(new Link<Void>("goChinese")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("zh", "CN"));
			}
		});
		add(new Link<Void>("goDanish")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().setLocale(new Locale("da", "DK"));
			}
		});
	}
}

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
package org.apache.wicket.examples.debug;

import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.debug.PageView;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.util.string.StringValueConversionException;

/**
 * A page that shows interesting attributes of the Wicket environment, including the current session
 * and the component tree for the current page.
 * 
 * @author Jonathan Locke
 */
public final class InspectorPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            The page id of any page to be analyzed
	 */
	public InspectorPage(final PageParameters parameters)
	{
		add(new ApplicationView("application", Application.get()));
		add(new SessionView("session", Session.get()));
		IPageMapEntry entry = null;
		try
		{
			entry = getPageMap().getEntry(parameters.getInt("pageId"));
		}
		catch (StringValueConversionException e)
		{
			// Ignore
		}
		add(new PageView("page", entry == null ? null : entry.getPage()));
		add(new Image("bug"));
		add(new BookmarkablePageLink("allsessions", LiveSessionsPage.class));
		add(new Label("wicketVersion", getApplication().getFrameworkSettings().getVersion()));
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

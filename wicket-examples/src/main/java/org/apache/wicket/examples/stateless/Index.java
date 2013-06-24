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
package org.apache.wicket.examples.stateless;

import org.apache.wicket.Session;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.StatelessLink;

/**
 * Index page of the stateless example.
 * 
 * @author Eelco Hillenius
 */
public class Index extends WicketExamplePage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public Index()
	{
		setStatelessHint(true);
		add(new Label("message", new SessionModel()));
		// First a normal bookmarkable link (which is stateless by default)
		add(new BookmarkablePageLink<>("linkToStatelessPage", StatelessPage.class));
		add(new BookmarkablePageLink<>("linkToStatelessPage1", StatelessPage1.class));
		add(new BookmarkablePageLink<>("linkToStatelessPage2", StatelessPage2.class));
		add(new BookmarkablePageLink<>("linkToStatelessPage3", StatelessPage3.class));
		// The second with a stateless link, so the onclick will be called but
		// on a stateless page.
		add(new StatelessLink<Void>("linkToStatefulPage")
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				setResponsePage(StatefulPage.class);
			}
		});
		add(new StatelessLink<Void>("invalidatesession")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				Session.get().invalidate();
				setResponsePage(Index.class);
			}

		});
	}
}
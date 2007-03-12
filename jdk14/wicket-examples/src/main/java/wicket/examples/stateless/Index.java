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
package wicket.examples.stateless;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.StatelessLink;

/**
 * Index page of the stateless example.
 * 
 * @author Eelco Hillenius
 */
public class Index extends WebPage
{
	/**
	 * Constructor
	 */
	public Index()
	{
		setStatelessHint(true);
		add(new Label("message", new SessionModel()));
		// First a normal bookmarkable link (which is stateless by default)
		add(new BookmarkablePageLink("linkToStatelessPage", StatelessPage.class));
		// The second with a stateless link, so the onclick will be called but on a stateless page.
		add(new StatelessLink("linkToStatefulPage")
		{
			/**
			 * @see wicket.markup.html.link.Link#onClick()
			 */
			public void onClick()
			{
				setResponsePage(StatefulPage.class);
			}
		});
	}
}
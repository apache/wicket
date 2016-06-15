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
package org.apache.wicket.examples.compref;

import org.apache.wicket.PageReference;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

/**
 * Example page that cannot be bookmarked. A page is bookmarkable when it has a public default
 * constructor and/or a constructor with a {@link org.apache.wicket.request.mapper.parameter.PageParameters} argument.
 * 
 * @author Eelco Hillenius
 */
public class NonBookmarkablePage extends WebPage
{
	/**
	 * Constructor.
	 * 
	 * @param referer
	 *            the refering page
	 */
	public NonBookmarkablePage(final PageReference referer)
	{
		if (referer == null)
		{
			throw new IllegalArgumentException("Argument referer must not be null");
		}

		// Add a link to navigate back to the refering page. We now use the
		// PageLink
		// constructor with the Page instance argument, because we already have
		// a page instance at our disposal
		add(Link.onClick("navigateBackLink", () -> setResponsePage(referer.getPage())));
	}
}

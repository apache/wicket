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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Example page that cannot be bookmarked. A page is bookmarkable when it has a public default
 * constructor and/or a public constructor with a {@link org.apache.wicket.request.mapper.parameter.PageParameters} argument.
 * 
 * @author Eelco Hillenius
 */
public class BookmarkablePage extends WebPage
{
	/**
	 * Constructor.
	 */
	public BookmarkablePage()
	{
		this(null);
		// note: its here for style actually. When you have both a default
		// constructor,
		// and a constructor with a PageParameters argument, the latter is
		// allways called
		// with a non-null argument
	}

	/**
	 * Construct.
	 * 
	 * @param pageParameters
	 */
	public BookmarkablePage(PageParameters pageParameters)
	{
		// get the message from the passed 'message' parameter or fall back to
		// the default
		// when no parameters were passed.

		String message;
		// note: the null check is here as a matter of defensive programming.
		// Actually, as this
		// constructor is allways called instead of the default constructor, and
		// pageParameters
		// never null, the check is not nescesarry. On the other hand... would
		// you ever trust
		// any API enough to just not check it at all?
		if (pageParameters == null ||
			pageParameters.get("message").toOptionalString() == null)
		{
			message = "This is the default message";
		}
		else
		{
			message = pageParameters.get("message").toOptionalString();
		}

		// Add a label to display the message
		add(new Label("messageLabel", message));

		// Add a link back. We did not hold any important instance data in
		// BookMarkabelPageLinkPage,
		// so navigating to a new instance is just fine
		add(new BookmarkablePageLink<>("navigateBackLink", BookmarkablePageLinkPage.class));
	}
}